(ns borsh.macros-test
  (:require [clojure.test :as t]
            [clojure.spec.alpha :as s]
            [borsh.macros :as sut]
            [borsh.types :refer [schema ->Variants]]))

(sut/defstruct Point [^:u64 x ^:u64 y])

(sut/defstruct
 Rect
 [^{:struct Point} a
  ^{:struct Point} b])

(sut/defstruct HasEnum [^{:enum [:e/a :e/b :e/c]} e])

(def enum-opts [:e/a :e/b :e/c])
(sut/defstruct HasEnum2 [^{:enum enum-opts} e])

(sut/defstruct Foo [^:u8 x])
(sut/defstruct Bar [^:u64 y])
(sut/defvariants FooBar [Foo Bar])
(sut/defstruct HasVariants [^{:enum FooBar} v])

(sut/defstruct HasOption [^{:option {:struct Point}} p])

(sut/defstruct HasVec [^{:vec {:struct Point}} ps])

(sut/defstruct HasMap [^{:map [:string :u64]} m])


(t/deftest test-defstruct
  (t/testing "Test defstruct"
    (t/is (= [[:x [:primitive :u64]] [:y [:primitive :u64]]] (schema (->Point 1 2))))
    (t/is (= [[:x [:primitive :u64]] [:y [:primitive :u64]]] (schema ->Point)))
    (t/is (= [[:a [:struct ->Point]]
              [:b [:struct ->Point]]]
             (schema ->Rect))))

  (t/testing "Test enum with keyword literals"
    (t/is (= [[:e [:enum [:e/a :e/b :e/c]]]]
             (schema ->HasEnum))))

  (t/testing "Test enum with symbol variable"
    (t/is (= [[:e [:enum [:e/a :e/b :e/c]]]]
             (schema ->HasEnum2))))

  (t/testing "Test variants"
    (t/is (= [[:v [:enum FooBar]]] (schema ->HasVariants))))

  (t/testing "Test option"
    (t/is (= [[:p [:option [:struct ->Point]]]] (schema ->HasOption))))

  (t/testing "Test map"
    (t/is (= [[:m [:map [[:primitive :string] [:primitive :u64]]]]]
             (schema ->HasMap))))

  )
