(ns borsh.reader-test
  (:refer-clojure :exclude [defstruct])
  (:require [clojure.test :as t]
            [borsh.reader :as sut]
            [borsh.macros :refer [defstruct defvariants]]
            [borsh.utils :as u])
  #?(:clj (:import clojure.lang.ExceptionInfo)))

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

(t/deftest test-deserialize
  (t/testing "u8 ~ u64"
    (let [bs (u/byte-array [1 2 0 3 0 0 0 4 0 0 0 0 0 0 0])
          x (->A 1 2 3 (u/bigint 4))]
      (t/is (= x (sut/deserialize ->A bs)))))

  (t/testing "Enum"
    (t/is (= (->B :e/a) (sut/deserialize ->B (u/byte-array [0]))))
    (t/is (= (->B2 :e/a) (sut/deserialize ->B2 (u/byte-array [0]))))
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
             (sut/deserialize ->F (u/byte-array [0 3 0 0 0 97 98 99])))))

  (t/testing "usize"
    (t/is (= (->J 10)
             (sut/deserialize ->J (u/byte-array [10 0 0 0 0 0 0 0]))))))
