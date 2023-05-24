(ns borsh.buffer
  (:require [borsh.types :refer [IBuffer]])
  (:import java.nio.ByteBuffer
           java.nio.ByteOrder
           java.util.Arrays))

(deftype Buffer [^ByteBuffer buf]
  IBuffer
  (to-byte-array [_this]
    (let [offset (.arrayOffset buf)]
      (Arrays/copyOfRange (.array buf) offset (+ offset (.position buf)))))
  (write-u8 [_this b]
    (.put buf (.byteValue b)))
  (write-u16 [_this n]
    (.putShort buf (.shortValue n)))
  (write-u32 [_this n]
    (.putInt buf (.intValue n)))
  (write-u64 [_this n]
    (.putLong buf n))
  (write-bytes [_this bs]
    (.put buf bs))
  (read-u8 [_this]
    (Byte/toUnsignedLong (.get buf)))
  (read-u16 [_this]
    (Short/toUnsignedLong (.getShort buf)))
  (read-u32 [_this]
    (Integer/toUnsignedLong (.getInt buf)))
  (read-u64 [_this]
    (.getLong buf))
  (read-bytes [_this length]
    (let [bs (byte-array length)]
      (.get buf bs 0 length)
      bs))
  (reset [_this]
    (.reset buf))
  (capacity [_this]
    (.capacity buf)))

(defn- make-buffer [^ByteBuffer buf]
  (doto buf (.order ByteOrder/LITTLE_ENDIAN) (.mark))
  (Buffer. buf))

(defn allocate
  [capacity]
  (let [buf (ByteBuffer/allocate capacity)]
    (make-buffer buf)))

(defn wrap
  [^bytes byte-array]
  (let [buf (ByteBuffer/wrap byte-array)] (make-buffer buf)))
