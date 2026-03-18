# jASTERIX test data (raw bytes)

These files are copied from:
- `C:\path\to\jASTERIX\src\test`

Into this project:
- `C:\path\to\AsterixDecoderNet\testdata\jasterix`

## Contents

- `cat001ed1.1.bin`
- `cat002ed1.0.bin`
- `cat004ed1.4.bin`
- `cat010ed0.24_sensis.bin`
- `cat010ed0.31.bin`
- `cat019ed1.3.bin`
- `cat020ed1.5.bin`
- `cat021ed0.26.bin`
- `cat021ed2.1.bin`
- `cat030ed7.0.bin`
- `cat034ed1.26.bin`
- `cat048ed1.15.bin`
- `cat048ed1.23.bin`
- `cat062ed1.12.bin`
- `cat062ed1.16.bin`
- `cat063ed1.0.bin`
- `cat065ed1.3.bin`
- `cat247ed1.2.bin`
- `cat252ed7.0.bin`

## Use with AsterixDecoderNet.Cli

From the project folder:

```powershell
cd C:\path\to\AsterixDecoderNet
```

Example CAT002:

```powershell
dotnet run --project .\AsterixDecoderNet.Cli\AsterixDecoderNet.Cli.csproj -- ".\testdata\jasterix\cat002ed1.0.bin" --limit 10 --json-out ".\out_cat002.json"
```

Example CAT021:

```powershell
dotnet run --project .\AsterixDecoderNet.Cli\AsterixDecoderNet.Cli.csproj -- ".\testdata\jasterix\cat021ed2.1.bin" --cat21-edition 2.1 --mapping 1.0 --limit 10 --json-out ".\out_cat021.json"
```

Example CAT062:

```powershell
dotnet run --project .\AsterixDecoderNet.Cli\AsterixDecoderNet.Cli.csproj -- ".\testdata\jasterix\cat062ed1.12.bin" --limit 10 --json-out ".\out_cat062.json"
```

## Note

These files are very small and ideal for quick parser regression tests.
For load/volume tests, please use larger `.ast` files.
