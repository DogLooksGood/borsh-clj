(ns borsh.buffer-test
  (:require [borsh.buffer :as sut]
            [borsh.types :as type]
            [clojure.test :as t]
            [borsh.utils :as u]))

(t/deftest test-buffer
  (t/testing "write and read u8"
    (let [buffer (sut/allocate 1)]
      (type/write-u8 buffer (byte 127))
      (type/reset buffer)
      (t/is (= 127 (type/read-u8 buffer)))))
  (t/testing "write and read u16"
    (let [buffer (sut/allocate 2)]
      (type/write-u16 buffer (short 20000))
      (type/reset buffer)
      (t/is (= 20000 (type/read-u16 buffer)))))
  (t/testing "write and read u32"
    (let [buffer (sut/allocate 4)]
      (type/write-u32 buffer (int 20000))
      (type/reset buffer)
      (t/is (= 20000 (type/read-u32 buffer)))))
  (t/testing "write and read u64"
    (let [buffer (sut/allocate 8)]
      (type/write-u64 buffer (u/bigint 20000))
      (type/reset buffer)
      (t/is (= (u/bigint 20000) (type/read-u64 buffer)))))
  (t/testing "write and read bytes"
    (let [buffer (sut/allocate 5)
          bytes (u/get-bytes "alice")]
      (type/write-bytes buffer bytes)
      (type/reset buffer)
      (let [ret (type/read-bytes buffer 5)]
        (t/is (= (vec bytes) (vec ret)))))))
