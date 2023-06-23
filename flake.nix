{
  description = "A basic Clojure/Script environment";

  inputs = {
    nixpkgs = { url = "github:NixOS/nixpkgs/nixpkgs-unstable"; };
    flake-utils = { url = "github:numtide/flake-utils"; };
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
      in
        {
          devShell = pkgs.mkShell {
            buildInputs = with pkgs; [
              jdk17_headless
              nodejs_18
              clojure
            ];
          };
        }
    );

  nixConfig = {
    bash-prompt-prefix = "[borsh-clj]";
  };
}
