(ns borsh.writer-test
  (:refer-clojure :exclude [defstruct])
  (:require [clojure.test :as t]
            [borsh.utils :as u]
            [borsh.writer :as sut]
            [borsh.types :as types]
            [borsh.buffer :as buffer]
            [borsh.macros :refer [defstruct defvariants]]
            [borsh.ext :as e])
  #?(:clj
     (:import clojure.lang.ExceptionInfo)))

(defstruct A [^:u8 a ^:u16 b ^:u32 c ^:u64 d])

(def enum-opts [:e/a :e/b])
(defstruct B [^{:enum [:e/a :e/b]} e])
(defstruct B2 [^{:enum enum-opts} e])

(defstruct C [^:u8 a ^{:struct B} b])

(defstruct D [^:string s])

(defstruct E [^{:option {:struct B}} b])

(defvariants DE [D E])

(defstruct F [^{:enum DE} x])

(defstruct G [^:bytes x])

(defstruct H [^{:vec {:struct D}} x])

(defstruct I [^{:map [:u8 :string]} x])

(defstruct J [^:usize x])

(defrecord IdExt [])
(extend-type IdExt
  e/IExtendWriter
  (write [this buf value]
    (let [v (byte (inc value))]
      (types/write-u8 buf v))))
(def id-ext (->IdExt))

(defstruct K [^{:ext id-ext} x])

(t/deftest test-serialize
  (t/testing "u8 ~ u64"
    (let [x (->A 1 2 3 (u/bigint 4))
          y (->A 255 65535 4294967295 (u/bigint 9223372036854775807))]
      (t/is (= [1 2 0 3 0 0 0 4 0 0 0 0 0 0 0]
               (vec (sut/serialize x))))
      ;; (t/is (= [-1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 127]
      ;;          (vec (sut/serialize y))))
      ))

  (t/testing "Enum"
    (let [x (->B :e/a)
          y (->B :x/a)
          z (->B2 :e/a)]
      (t/is (= [0] (vec (sut/serialize x))))
      (t/is (= [0] (vec (sut/serialize z))))
      (t/is (thrown? ExceptionInfo #"Invalid enum value"
                     (sut/serialize y)))))

  (t/testing "Struct"
    (let [x (->C 10 (->B :e/b))]
      (t/is (= [10 1] (vec (sut/serialize x))))))

  (t/testing "String"
    (let [x (->D "abc")]
      (t/is (= [3 0 0 0 97 98 99] (vec (sut/serialize x))))))

  (t/testing "Option"
    (let [x (->E nil)
          y (->E (->B :e/b))]
      (t/is (= [0] (vec (sut/serialize x))))
      (t/is (= [1 1] (vec (sut/serialize y))))))

  (t/testing "Variants"
    (let [x (->F (->D "abc"))]
      (t/is (= [0 3 0 0 0 97 98 99] (vec (sut/serialize x))))))

  (t/testing "Bytes"
    (let [x (->G (u/byte-array [1 2 3]))]
      (t/is (= [3 0 0 0 1 2 3] (vec (sut/serialize x))))))

  (t/testing "Vec of structs"
    (let [x (->H [(->D "abc")
                  (->D "def")])]
      (t/is (= [2 0 0 0 3 0 0 0 97 98 99 3 0 0 0 100 101 102]
               (vec (sut/serialize x))))))

  (t/testing "Map"
    (let [x (->I {1 "a" 2 "b"})]
      (t/is (= [2 0 0 0 1 1 0 0 0 97 2 1 0 0 0 98]
               (vec (sut/serialize x))))))

  (t/testing "usize"
    (let [x (->J 10)]
      (t/is (= [10 0 0 0 0 0 0 0]
               (vec (sut/serialize x))))))

  (t/testing "ext"
    (let [x (->K 0)]
      (t/is (= [1]
               (vec (sut/serialize x)))))))
