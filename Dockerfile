FROM nixos/nix:latest

WORKDIR /app

COPY flake.lock /app/flake.lock
COPY flake.nix /app/flake.nix

# Needed to run sbt
RUN nix --experimental-features 'nix-command flakes' profile install nixpkgs#busybox

COPY . /app

#openjdk-19.0.2+7
ENTRYPOINT [ "nix", "--extra-experimental-features", "nix-command flakes", "develop" ]