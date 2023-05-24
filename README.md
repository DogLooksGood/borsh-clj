# borsh-clj [![Test](https://github.com/DogLooksGood/borsh-clj/actions/workflows/test.yml/badge.svg)](https://github.com/DogLooksGood/borsh-clj/actions/workflows/test.yml)
A pure Clojure/Script implementation for Borsh

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
| UTF-8 String             | String               | `^:string`                    |
| Option                   | `nil` or type        | `^{:option type}`             |
| Vec                      | Vector               | `^{:vec item-type}`           |
| Map                      | HashMap              | `^{:map [key-type val-type]}` |
| Structs                  | Record               | `^{:struct record}`           |
| Simple enums             | Keyword              | `^{:enums [kws]}`             |
| Enum of structs          | Record               | `^{:variants variants}`       |
| Dynamic-sized byte array | Byte[] or Uint8Array | `^:bytes`                     |
