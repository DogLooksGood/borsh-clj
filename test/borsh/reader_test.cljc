(ns borsh.reader-test
  (:refer-clojure :exclude [defrecord])
  (:require [clojure.test :as t]
            [borsh.reader :as sut]
            [borsh.macros :refer [defrecord defvariants]]
            [borsh.utils :as u])
  #?(:clj (:import clojure.lang.ExceptionInfo)))

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

(t/deftest test-deserialize
  (t/testing "u8 ~ u64"
    (let [bs (u/byte-array [1 2 0 3 0 0 0 4 0 0 0 0 0 0 0])
          x (->A 1 2 3 (u/bigint 4))]
      (t/is (= x (sut/deserialize ->A bs)))))

  (t/testing "Enum"
    (t/is (= (->B :e/a) (sut/deserialize ->B (u/byte-array [0]))))
    (t/is (thrown? ExceptionInfo #"Invalid enum value: 2"
                   (sut/deserialize ->B (u/byte-array [2])))))

  (t/testing "Struct"
    (let [x (->C 10 (->B :e/b))
          bs (u/byte-array [10 1])]
      (t/is (= x (sut/deserialize ->C bs)))))

  (t/testing "String"
    (let [bs (u/byte-array [3 0 0 0 97 98 99])
          x (->D "abc")]
      (t/is (= x (sut/deserialize ->D bs)))))

  (t/testing "Option"
    (t/is (= (->E nil) (sut/deserialize ->E (u/byte-array [0]))))
    (t/is (= (->E (->B :e/b)) (sut/deserialize ->E (u/byte-array [1 1])))))

  (t/testing "Variants"
    (t/is (= (->F (->D "abc"))
             (sut/deserialize ->F (u/byte-array [0 3 0 0 0 97 98 99]))))))
