(ns borsh.writer-test
  (:refer-clojure :exclude [defrecord])
  (:require [clojure.test :as t]
            [borsh.utils :as u]
            [borsh.writer :as sut]
            [borsh.macros :refer [defrecord defvariants]])
  #?(:clj
     (:import clojure.lang.ExceptionInfo)))

(defrecord A [^:u8 a ^:u16 b ^:u32 c ^:u64 d])

(defrecord B [^{:enum [:e/a :e/b]} e])

(defrecord C [^:u8 a ^{:struct B} b])

(defrecord D [^:string s])

(defrecord E [^{:option {:struct B}} b])

(defvariants DE [D E])

(defrecord F [^{:variants DE} x])

(defrecord G [^:bytes x])

(defrecord H [^{:vec {:struct D}} x])

(defrecord I [^{:map [:u8 :string]} x])

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
          y (->B :x/a)]
      (t/is (= [0] (vec (sut/serialize x))))
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
               (vec (sut/serialize x)))))))
