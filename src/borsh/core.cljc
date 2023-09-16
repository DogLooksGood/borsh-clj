(ns borsh.core
  (:require [borsh.writer :as w]
            [borsh.reader :as r]
            [borsh.types :as t]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn schema
  "Get the schema from an object or a contructor function."
  [obj-or-ctor]
  (t/schema obj-or-ctor))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn serialize
  "Serialize obj into a byte array."
  ([obj]
   (w/serialize obj))
  ([obj schema]
   (w/serialize obj schema))
  ([obj schema opts]
   (w/serialize obj schema opts)))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn deserialize
  "Deserialize a byte array into an object."
  ([ctor data]
   (r/deserialize ctor data))
  ([ctor data schema]
   (r/deserialize ctor data schema)))
