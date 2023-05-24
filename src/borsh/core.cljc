(ns borsh.core
  (:require [borsh.writer :as w]
            [borsh.reader :as r]
            [borsh.types :as t]))

(defn schema
  "Get the schema from an object or a contructor function."
  [obj-or-ctor]
  (t/schema obj-or-ctor))

(defn serialize
  "Serialize obj into a byte array."
  ([obj]
   (w/serialize obj))
  ([obj schema]
   (w/serialize obj schema))
  ([obj schema opts]
   (w/serialize obj schema opts)))

(defn deserialize
  "Deserialize a byte array into an object."
  ([ctor data]
   (r/deserialize ctor data))
  ([ctor data schema]
   (r/deserialize ctor data schema)))
