(ns borsh.writer
  (:require [borsh.types :as t]
            [borsh.buffer :as b]
            [borsh.utils :as u]
            [borsh.macros :as m]))

(declare serialize-struct)

(defn serialize-value [buf value schema]
  (let [[t opts] schema]
    (case t
      :primitive
      (case opts
        :bool (t/write-u8 buf (if (true? value) 1 0))
        :u8 (if (<= 0 value 255)
              (t/write-u8 buf value)
              (throw (ex-info (str "Invalid u8 value: " value) {})))
        :u16 (if (<= 0 value 65535)
               (t/write-u16 buf value)
               (throw (ex-info (str "Invalid u16 value: " value) {})))
        :u32 (if (<= 0 value 4294967295)
               (t/write-u32 buf value)
               (throw (ex-info (str "Invalid u32 value: " value) {})))
        :u64 (t/write-u64 buf value)
        :string (do
                  (t/write-u32 buf (count value))
                  (t/write-bytes buf (u/get-bytes value)))
        :bytes (do
                 (t/write-u32 buf (u/count-byte-array value))
                 (t/write-bytes buf value)))

      :vec
      (do
        (t/write-u32 buf (count value))
        (doseq [it value]
          (serialize-value buf it opts)))

      :map
      (do
        (t/write-u32 buf (count value))
        (doseq [[k v] value]
          (serialize-value buf k (first opts))
          (serialize-value buf v (second opts))))

      :enum
      (let [idx (.indexOf opts value)]
        (when (= -1 idx)
          (throw (ex-info (str "Invalid enum value: " value) {:enum opts})))
        (t/write-u8 buf (byte idx)))

      :struct
      (let [schema (t/schema opts)]
        (serialize-struct buf value schema))

      :option
      (if (nil? value)
        (t/write-u8 buf (byte 0))
        (do (t/write-u8 buf (byte 1))
            (serialize-value buf value opts)))

      :variants
      (let [{:keys [types]} opts
            idx (.indexOf types (type value))]
        (if (= -1 idx)
          (throw (ex-info "Invalid variant" {:value value :variants types}))
          (do
            (t/write-u8 buf (byte idx))
            (serialize-struct buf value (t/schema value))))))))

(defn serialize-struct [buf obj schema]
  (doseq [[key field] schema
          :let [v (get obj key)]]
    (serialize-value buf v field)))

(defn serialize
  "Serialize obj according to schema, return a byte array."
  ([obj]
   (let [schema (t/schema obj)]
     (serialize obj schema)))
  ([obj schema]
   (serialize obj schema {:buffer-size 4096}))
  ([obj schema {:keys [buffer-size]}]
   (let [buf (b/allocate buffer-size)]
     (serialize-struct buf obj schema)
     (t/to-byte-array buf))))
