(ns borsh.utils
  (:refer-clojure :exclude [bigint byte-array]))

#?(:cljs
   (do
     (def text-encoder (js/TextEncoder.))
     (def text-decoder (js/TextDecoder. "utf8"))))

(defn bigint [n]
  #?(:cljs (js/BigInt n)
     :clj n))

(defn get-bytes [s]
  #?(:clj (.getBytes s)
     :cljs (.encode text-encoder s)))

(defn bytes->str [bs]
  #?(:clj (String. bs)
     :cljs (.decode text-decoder bs)))

(defn byte-array [v]
  #?(:cljs (js/Uint8Array.from v)
     :clj (clojure.core/byte-array v)))

(defn count-byte-array [bs]
  #?(:clj (count bs)
     :cljs (.-length bs)))
