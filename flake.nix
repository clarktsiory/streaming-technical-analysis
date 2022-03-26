{
  inputs = {
    typelevel-nix.url = "github:typelevel/typelevel-nix";
    nixpkgs.url = "nixpkgs/nixos-unstable"; # NOTE we need latest curl
    flake-utils.follows = "typelevel-nix/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils, typelevel-nix }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          overlays = [ typelevel-nix.overlay ];
        };
        curl = pkgs.curl.overrideAttrs (old: {
          # Websocket support in curl is currently in experimental mode
          # which needs to be enabled explicitly in build time in order to be available
          #
          #https://github.com/curl/curl/blob/f8da4f2f2d0451dc0a126ae3e5077b4527ccdc86/configure.ac#L174
          configureFlags = old.configureFlags ++ [ "--enable-websockets" ];
        });

        talib = pkgs.ta-lib;

        mkShell = jdk:
          pkgs.devshell.mkShell {
            imports = [ typelevel-nix.typelevelShell ];
            name = "streaming-technical-analysis";
            typelevelShell = {
              jdk.package = jdk;
              native = {
                enable = true;
                libraries = [ curl pkgs.nghttp2 talib pkgs.s2n-tls pkgs.boehmgc ];
              };
            };

            # add talib, fix typelevel export variables https://github.com/typelevel/typelevel-nix/issues/107
            env = [ 
              { 
                name = "LIBRARY_PATH"; 
                eval = "$DEVSHELL_DIR/lib"; 
              } 
              { 
                name = "C_INCLUDE_PATH"; 
                eval = "$DEVSHELL_DIR/include"; 
              } 
            ];
          };
      in {
        devShell = mkShell pkgs.jdk17;

        devShells = {
          "temurin@8" = mkShell pkgs.temurin-bin-8;
          "temurin@17" = mkShell pkgs.temurin-bin-17;
        };
      });
}