(ns borsh.macros
  (:refer-clojure :exclude [defrecord])
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [borsh.specs :as specs]
            [borsh.types :refer [schema ->Variants]]))

(defn sym->ctor [sym]
  (let [n (namespace sym)
        s (name sym)
        f (if (str/starts-with? s "->")
            s
            (str "->" s))]
    (symbol n f)))

(defn unify-meta [m]
  (let [ps (filter m specs/primitives)]
    (s/conform ::specs/type
               (case (count ps)
                 1 (first ps)
                 0 m
                 (throw (ex-info "Invalid meta attached to field" {:meta m}))))))

(defn parse-field-schema [s]
  (let [[s-type s-value] s]
    (condp = s-type
      :primitive s
      :enum [:enum (:enum s-value)]
      :bytes [:bytes (:bytes s-value)]
      :struct [:struct (sym->ctor (:struct s-value))]
      :option [:option (parse-field-schema (:option s-value))]
      :vec [:vec (parse-field-schema (:vec s-value))]
      :variants [:variants (:variants s-value)]
      :map (let [[k v] (:map s-value)]
             [:map
              [(parse-field-schema k)
               (parse-field-schema v)]])
      [s-type (val (first s-value))])))

(defn parse-field [f]
  (let [m (meta f)
        s (unify-meta m)]
    (when (s/invalid? s)
      (throw (ex-info "Invalid field type" {:field-name f
                                            :field-meta m})))
    [(keyword f)
     (parse-field-schema s)]))

(defn parse-schema
  "Return schema based on fields' metadata."
  [fields]
  (let [schema (mapv parse-field fields)]
    schema))

(defn defrecord-impl [env name fields]

  (let [schema   (parse-schema fields)
        ctor-sym (symbol (str "->" name))
        is-cljs (some? (:ns env))]
    (if is-cljs
      `(do
         (clojure.core/defrecord ~name [~@fields])
         (extend-type ~name
           borsh.types/HasSchema
           (borsh.types/-schema [this#] ~schema))
         (aset ~ctor-sym "prototype" "borshSchema" ~schema))
      `(do
         (clojure.core/defrecord ~name [~@fields])
         (extend-type ~name
           borsh.types/HasSchema
           (borsh.types/-schema [this#] ~schema))
         (extend-type (class ~ctor-sym)
           borsh.types/HasSchema
           (borsh.types/-schema [this#] ~schema))))))

(defmacro defrecord
  "This macro works similarly to clojure.core/defrecord, except borsh
  schema will be defined at the same time. Generated class and
  constructor function(e.g. ->Foo) are extended to protocol HasSchema,
  so both (schema Foo) and (schema ->Foo) return the schema."
  [name fields]
  (defrecord-impl &env name fields))

(defn defvariants-impl [name variants]
  `(do
     (def ~name
       (->Variants
        [~@(mapv sym->ctor variants)]
        ~variants))))

(defmacro defvariants
  "This macro creates a schema of variants to represent the complex enums."
  [name variants]
  (defvariants-impl name variants))
