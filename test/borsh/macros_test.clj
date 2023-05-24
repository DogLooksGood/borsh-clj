(ns borsh.macros-test
  (:require [clojure.test :as t]
            [clojure.spec.alpha :as s]
            [borsh.macros :as sut]
            [borsh.types :refer [schema ->Variants]]))

(sut/defrecord Point [^:u64 x ^:u64 y])

(sut/defrecord
 Rect
 [^{:struct Point} a
  ^{:struct Point} b])

(sut/defrecord HasEnum [^{:enum [:e/a :e/b :e/c]} e])

(sut/defrecord HasOption [^{:option {:struct Point}} p])

(sut/defrecord HasVec [^{:vec {:struct Point}} ps])

(sut/defrecord HasMap [^{:map [:string :u64]} m])

(sut/defrecord Foo [^:u8 x])
(sut/defrecord Bar [^:u64 y])
(sut/defvariants FooBar [Foo Bar])
(sut/defrecord HasVariants [^{:variants FooBar} v])

(t/deftest test-defrecord
  (t/testing "Test defrecord"
    (t/is (= [[:x [:primitive :u64]] [:y [:primitive :u64]]] (schema (->Point 1 2))))
    (t/is (= [[:x [:primitive :u64]] [:y [:primitive :u64]]] (schema ->Point)))
    (t/is (= [[:a [:struct ->Point]]
              [:b [:struct ->Point]]]
             (schema ->Rect))))

  (t/testing "Test enum"
    (t/is (= [[:e [:enum [:e/a :e/b :e/c]]]]
             (schema ->HasEnum))))

  (t/testing "Test option"
    (t/is (= [[:p [:option [:struct ->Point]]]] (schema ->HasOption))))

  (t/testing "Test map"
    (t/is (= [[:m [:map [[:primitive :string] [:primitive :u64]]]]]
             (schema ->HasMap))))

  (t/testing "Test variants"
    (t/is (= [[:v [:variants FooBar]]] (schema ->HasVariants)))))
