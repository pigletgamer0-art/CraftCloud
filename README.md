# CraftCloud — aplicación Android (compilación de prueba)

CraftCloud: Grandes mundos comienzan aquí.

Este repositorio contiene una aplicación Android de prueba que permite crear perfiles locales de servidores Minecraft, gestionar su configuración, registrar múltiples mods seleccionados desde Android y consultar el catálogo de Modrinth. **No crea servidores reales en la nube, no inicia servidores ni vincula una cuenta Microsoft.** El propietario del perfil es una etiqueta local, no un usuario autenticado.

Este repositorio es público. **Nunca subas credenciales Microsoft, tokens de hosting ni datos personales.**

## APK de prueba
Ve a **Actions → Compilar APK de CraftCloud → ejecución más reciente → Artifacts → CraftCloud-debug-APK**. Descarga el ZIP y extrae `app-debug.apk`.

La APK resultante está firmada con clave de depuración y solo es para pruebas. Para una versión de producción hacen falta un backend, autenticación real, infraestructura y firma de publicación.

La aplicación Android es una base funcional inicial. El proyecto v0.5 completo del chat contiene otros archivos; aquí se integran primero las funciones locales esenciales para obtener una APK instalable.
