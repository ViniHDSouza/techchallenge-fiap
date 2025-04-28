@echo off
setlocal enabledelayedexpansion

for /R %%f in (*.java,*.properties, *.yml) do (
    type "%%f" >> fontes.txt
)

echo Conteudo dos arquivos .java copiados para fontes.txt
pause