(ns borsh.reader
  (:require [borsh.types :as t]
            [borsh.buffer :as b]
            [borsh.utils :as u])
  #?(:clj
     (:import clojure.lang.ExceptionInfo
              borsh.types.Variants)))

(declare deserialize-struct)

(defn deserialize-value [buf schema path]
  (let [[t opts] schema]
    (case t
      :primitive
      (case opts
        :bool
        (let [v (t/read-u8 buf)]
          (not= v 0))
        :u8
        (t/read-u8 buf)
        :u16
        (t/read-u16 buf)
        :u32
        (t/read-u32 buf)
        :u64
        (t/read-u64 buf)
        :usize
        (u/number (t/read-u64 buf))
        :string
        (let [c (t/read-u32 buf)
              bs (t/read-bytes buf c)]
          (u/bytes->str bs))
        :bytes
        (let [c (t/read-u32 buf)
              bs (t/read-bytes buf c)]
          bs))

      :enum
      (if (instance? #?(:clj Variants :cljs t/Variants) opts)
        (let [{:keys [ctors]} opts
              idx (t/read-u8 buf)]
          (if (< idx (count ctors))
            (let [ctor (nth ctors idx)]
              (deserialize-struct buf ctor (t/schema ctor) (conj path [:variant idx])))
            (throw (ex-info (str "Invalid variant prefix: " idx) {:range (count ctors)}))))
        (let [idx (t/read-u8 buf)]
          (if (< idx (count opts))
            (nth opts idx)
            (throw (ex-info (str "Invalid enum value: " idx) {:range (count opts)})))))

      :vec
      (let [c (t/read-u32 buf)]
        (->> (for [i (range c)]
               (deserialize-value buf opts (conj path i)))
             (into [])))

      :map
      (let [c (t/read-u32 buf)]
        (->> (for [i (range c)
                   :let [[k v] opts]]
               [(deserialize-value buf k (conj path [:entry-key i]))
                (deserialize-value buf v (conj path [:entry-value i]))])
             (into {})))

      :struct
      (let [schema (t/schema opts)]
        (deserialize-struct buf opts schema (conj path :struct)))

      :option
      (let [b (t/read-u8 buf)]
        (when-not (zero? b)
          (deserialize-value buf opts (conj path :option)))))))

(defn deserialize-struct [buf ctor schema path]
  (->> (for [[k field] schema]
         (deserialize-value buf field (conj path k)))
       (apply ctor)))

(defn deserialize
  "Deserialize data according to schema, return an object record."
  ([ctor data]
   (let [schema (t/schema ctor)]
     (deserialize ctor data schema)))
  ([ctor data schema]
   (let [buf (b/wrap data)]
     (deserialize-struct buf ctor schema []))))
