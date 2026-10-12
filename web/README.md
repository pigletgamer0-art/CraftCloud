# CraftCloud — siguiente etapa: plataforma web de hosting

Este directorio contiene la primera interfaz web adaptable del proyecto CraftCloud.

## Estado real
- [x] Interfaz adaptable para escritorio y móvil.
- [x] Crear/eliminar registros de configuración guardados en el navegador.
- [x] Validación básica de nombres y mensajes de estado.
- [ ] Autenticación de usuarios en un backend.
- [ ] Base de datos y aislamiento por propietario.
- [ ] Servicio de orquestación para procesos de servidor.
- [ ] Aprovisionamiento real de Minecraft Bedrock Dedicated Server.
- [ ] Consola conectada a registros reales, detener/reiniciar, límites de recursos y copias de seguridad.
- [ ] Pruebas automatizadas, límites de abuso y despliegue de producción.

## Ejecutar la vista previa
Abre `index.html` en un navegador moderno. No requiere dependencias.

## Importante
Esta vista previa **no aloja ni inicia servidores Minecraft**. Los registros se guardan únicamente en el almacenamiento local del navegador. No introduzcas secretos ni credenciales aquí. Los botones de crear/eliminar solo gestionan esos registros locales.

Para producción se necesita un backend persistente y un host de servidores capaz de ejecutar procesos de larga duración. No se debe ejecutar código de servidor enviado por usuarios dentro de una función web serverless. El backend debe autenticar cada petición, comprobar la propiedad del servidor y aplicar cuotas/aislamiento antes de enviar tareas al agente de hosting.

## Siguiente paso técnico recomendado
1. Definir y desplegar el backend de autenticación y metadatos.
2. Implementar un agente de hosting separado en una máquina compatible.
3. Conectar la API del panel al agente con autenticación entre servicios.
4. Probar ciclo de vida, límites, persistencia y acceso desde Minecraft antes de anunciarlo como listo.
