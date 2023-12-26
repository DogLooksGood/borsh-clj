# borsh-clj [![Test](https://github.com/DogLooksGood/borsh-clj/actions/workflows/test.yml/badge.svg)](https://github.com/DogLooksGood/borsh-clj/actions/workflows/test.yml) [![Clojars Project](https://img.shields.io/clojars/v/io.github.doglooksgood/borsh-clj.svg)](https://clojars.org/io.github.doglooksgood/borsh-clj)
A pure Clojure/Script implementation for [Borsh](https://borsh.io/), the binary serializer for security-critical projects.

## Examples

```clojure
(require '[borsh.macros :as m]
         '[borsh.core :as borsh])

;; Define schemas
(m/defstruct Point
    [^:u32 x
     ^:u32 y])

(m/defstruct Rect
    [^{:struct Point} p1
     ^{:struct Point} p2])

(def r (->Rect
        (->Point 0 0)
        (->Point 42 42)))

;; Serialize an object
(def bs (borsh/serialize r))

;; Deserialize a byte array
(def r1 (borsh/deserialize ->Rect bs))
```

## Type Mappings

| Borsh                    | Clojure              | Meta                          |
|--------------------------|----------------------|-------------------------------|
| boolean                  | Boolean              | `^:bool`                      |
| `u8` integer             | Long or Number       | `^:u8`                        |
| `u16` integer            | Long or Number       | `^:u16`                       |
| `u32` integer            | Long or Number       | `^:u32`                       |
| `u64` integer            | Long or BigInt       | `^:u64`                       |
| `usize` integer          | Long or Number       | `^:usize`                     |
| UTF-8 String             | String               | `^:string`                    |
| Option                   | `nil` or type        | `^{:option type}`             |
| Vec                      | Vector               | `^{:vec item-type}`           |
| Map                      | HashMap              | `^{:map [key-type val-type]}` |
| Struct                   | Record               | `^{:struct record}`           |
| Simple enum              | Keyword              | `^{:enum [kws]}`              |
| Enum of structs          | Record               | `^{:enum variants}`           |
| Dynamic-sized byte array | Byte[] or Uint8Array | `^:bytes`                     |
| Extended type            |                      | `^{:ext ext}`                 |

## Enums

Simple enums can be defined with a vector of keywords

```clojure
(require '[borsh.macros :as m])

(m/defstruct HasStatus
  [^{:enum [:status/a :status/b]} status])

;; Another approach
(def status-enums [:status/a :status/b])
(m/defstruct HasStatus
  [^{:enum status-enums} status])
```

Complicated enums can be defined with `borsh.macro/defvariants`.

```clojure
(require '[borsh.macros :as m])

(m/defstruct Point [^:u64 x ^:u64 y])
(m/defstruct Line [^{:struct Point} p1 ^{:struct Point} p2])

(m/defvariants shapes [Point Line])
(m/defstruct HasVariants
  [^{:enum shapes} shape])
```

## Extended Types

To extend a custom type, use `IExtendWriter` and `IExtendReader`.

```clojure
(require '[borsh.ext :as ext]
         '[borsh.types :as t]
         '[borsh.macros :as m])

(defrecord DateExt [])

(extend-type DateExt
  ext/IExtendWriter
  (write [this buf value] ;; the type of value is java.util.Date
    (t/write-u64 buf (.getTime value)))
  ext/IExtendReader
  (read [this buf]        ;; return java.util.Date
    (java.util.Date. (t/read-u64 buf))))

(def date-ext (->DateExt))

(m/defstruct X [^{:ext date-ext} date])
```
