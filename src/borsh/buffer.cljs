(ns borsh.buffer
  (:require [borsh.types :refer [IBuffer]]))

(deftype Buffer [^js view offset]
  IBuffer
  (to-byte-array [_this]
    (js/Uint8Array. (.slice (.-buffer view) 0 offset)))
  (write-u8 [this b]
    (.setUint8 view offset b)
    (set! (.-offset this) (inc offset)))
  (write-u16 [this n]
    (.setUint16 view offset n true)
    (set! (.-offset this) (+ offset 2)))
  (write-u32 [this n]
    (.setUint32 view offset n true)
    (set! (.-offset this) (+ offset 4)))
  (write-u64 [this n]
    (.setBigUint64 view offset n true)
    (set! (.-offset this) (+ offset 8)))
  (write-bytes [this bs]
    (doseq [b bs]
      (.setUint8 view offset b)
      (set! (.-offset this) (inc offset))))
  (read-u8 [this]
    (let [v (.getUint8 view offset)]
      (set! (.-offset this) (inc offset))
      v))
  (read-u16 [this]
    (let [v (.getUint16 view offset true)]
      (set! (.-offset this) (+ offset 2))
      v))
  (read-u32 [this]
    (let [v (.getUint32 view offset true)]
      (set! (.-offset this) (+ offset 4))
      v))
  (read-u64 [this]
    (let [v (.getBigUint64 view offset true)]
      (set! (.-offset this) (+ offset 8))
      v))
  (read-bytes [this length]
    (let [v (js/Uint8Array. (.slice (.-buffer view) offset (+ offset length)))]
      (set! (.-offset this) (+ offset length))
      v))
  (reset [this]
    (set! (.-offset this) 0))
  (capacity [_this]
    (.-length (.-buffer view))))

(defn- make-buffer [buf]
  (Buffer. (js/DataView. buf) 0))

(defn allocate [capacity]
  (let [buf (js/ArrayBuffer. capacity)]
    (make-buffer buf)))

(defn wrap [bs]
  (make-buffer (.-buffer bs)))
