(ns borsh.ext
  (:refer-clojure :exclude [read]))

(defprotocol IExtendWriter
  (write [this buf value]))

(defprotocol IExtendReader
  (read [this buf]))
