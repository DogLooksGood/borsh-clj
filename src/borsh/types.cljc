(ns borsh.types)

(defrecord Variants [ctors types])

(defprotocol HasSchema
  (-schema [this]
    "Get the borsh schema for current type."))

(defn schema [x]
  #?(:clj
     (-schema x)
     :cljs
     (if-let [s (some-> x (aget "prototype") (aget "borshSchema"))]
       s
       (-schema x))))

(extend-type Variants
  HasSchema
  (-schema [this]
    [:enum (:ctors this)]))

(defprotocol IBuffer
  (to-byte-array [this])
  (write-u8 [this b])
  (write-u16 [this n])
  (write-u32 [this n])
  (write-u64 [this n])
  (write-bytes [this bs])
  (read-u8 [this])
  (read-u16 [this])
  (read-u32 [this])
  (read-u64 [this])
  (read-bytes [this length])
  (reset [this])
  (capacity [this]))
