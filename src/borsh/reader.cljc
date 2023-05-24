(ns borsh.reader
  (:require [borsh.types :as t]
            [borsh.buffer :as b]
            [borsh.utils :as u]))

(declare deserialize-struct)

(defn deserialize-value [buf schema]
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
        :string
        (let [c (t/read-u32 buf)
              bs (t/read-bytes buf c)]
          (u/bytes->str bs))
        :bytes
        (let [c (t/read-u32 buf)
              bs (t/read-bytes buf c)]
          bs))

      :enum
      (let [idx (t/read-u8 buf)]
        (if (< idx (count opts))
          (nth opts idx)
          (throw (ex-info (str "Invalid enum value: " idx) {:range (count opts)}))))

      :vec
      (let [c (t/read-u32 buf)]
        (->> (for [_i (range c)]
               (deserialize-value buf opts))
             (into [])))

      :map
      (let [c (t/read-u32 buf)]
        (->> (for [_i (range c)]
               [(deserialize-value buf (first opts))
                (deserialize-value buf (second opts))])
             (into {})))

      :struct
      (let [schema (t/schema opts)]
        (deserialize-struct buf opts schema))

      :option
      (let [b (t/read-u8 buf)]
        (when-not (zero? b)
          (deserialize-value buf opts)))

      :variants
      (let [{:keys [ctors]} opts
            idx (t/read-u8 buf)]
        (if (< idx (count ctors))
          (let [ctor (nth ctors idx)]
            (deserialize-struct buf ctor (t/schema ctor)))
          (throw (ex-info (str "Invalid variant prefix: " idx) {:range (count ctors)})))))))

(defn deserialize-struct [buf ctor schema]
  (->> (for [[_ field] schema]
         (deserialize-value buf field))
       (apply ctor)))

(defn deserialize
  "Deserialize data according to schema, return an object record."
  ([ctor data]
   (let [schema (t/schema ctor)]
     (deserialize ctor data schema)))
  ([ctor data schema]
   (let [buf (b/wrap data)]
     (deserialize-struct buf ctor schema))))
