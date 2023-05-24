(ns borsh.specs
  (:require [clojure.spec.alpha :as s]
            [borsh.types :refer [HasSchema]]))

(def primitives #{:bool :u8 :u16 :u32 :u64 :string :bytes})

(s/def ::primitive primitives)
(s/def ::type
  (s/or :primitive ::primitive
        :vec (s/keys :req-un [::vec])
        :enum (s/keys :req-un [::enum])
        :struct (s/keys :req-un [::struct])
        :option (s/keys :req-un [::option])
        :bytes (s/keys :req-un [::bytes])
        :map (s/keys :req-un [::map])
        :variants (s/keys :req-un [::variants])))
(s/def ::option ::type)
(s/def ::vec ::type)
(s/def ::enum (s/coll-of keyword? :kind vector?))
(s/def ::struct symbol?)
(s/def ::bytes pos-int?)
(s/def ::map (s/tuple ::type ::type))
(s/def ::variants symbol?)
