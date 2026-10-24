# Basketball Mod - Forge 1.20.1

## Obtener el .jar (sin instalar nada)
1. Crea una cuenta en github.com y un repositorio nuevo (privado esta bien).
2. Sube TODO el contenido de esta carpeta (incluida la carpeta .github): "Add file > Upload files".
3. Abre la pestana "Actions", entra a "Compilar mod" y espera ~5 min (marca verde).
4. Dentro de esa ejecucion, abajo en "Artifacts", descarga "basketball-mod-jar". Descomprime: el .jar es el mod.
5. Copia el .jar a %appdata%\.minecraft\mods y abre Minecraft con Forge 1.20.1.

## Compilar en tu PC (alternativa)
JDK 17 + Gradle 8.1.1, en esta carpeta: gradle build. El jar queda en build/libs.

## Jugar
- Balon: clic derecho (mantener) = cargar tiro, soltar = tirar. Bandeja automatica corriendo/saltando cerca del aro.
- C = driblar con UN toque (el balon sigue solo hasta que toques C otra vez, tires o pierdas el balon).
- V = cambiar movimiento, G = usar movimiento. Los ofensivos piden balon; los defensivos no:
  Marcaje (pegarse al rival con balon), Robo de balon (rival enfrente y cerca), Tapon (salto que desvia tiros cercanos), Postura defensiva (mas chances de robo por 4 s).
- /basket court = guia para construir la cancha. /basket court build <parquet|cemento|asfalto|goma> (op) la construye sola.
- /basket shoes = tabla de zapatillas. /basket moves, /basket points.
- /challenger <jugador>, /challenger accept, /score = partidos 1v1 a 21.
- Todas las recetas se desbloquean solas en el libro de recetas.
