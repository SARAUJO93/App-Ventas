# Configuración de Seguridad y Firma de Lanzamiento (CI/CD)

## 1. Firma de Producción en GitHub Actions

Para asegurar que las versiones de producción publicadas vía GitHub Releases utilicen una clave privada única y persistente (evitando `debug.keystore`), se debe configurar la firma en **GitHub Secrets**:

### Pasos:
1. Generar la clave privada de producción localmente:
   ```bash
   keytool -genkey -v -keystore release.keystore -alias novapos -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Convertir el archivo `release.keystore` a Base64:
   ```powershell
   [Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore"))
   ```
3. En GitHub Repository > **Settings > Secrets and variables > Actions**, agregar los siguientes secretos:
   - `RELEASE_KEYSTORE_BASE64`: Contenido Base64 de la clave.
   - `RELEASE_STORE_PASSWORD`: Contraseña de la clave.
   - `RELEASE_KEY_ALIAS`: Alias `novapos`.
   - `RELEASE_KEY_PASSWORD`: Contraseña del alias.

4. El archivo `keystore.properties` generado durante el build en CI leerá estas variables sin exponerlas en el repositorio.

---

## 2. Reglas de Seguridad en Cloud Firestore

Las reglas de seguridad se encuentran publicadas en `firestore.rules`:

```protobuf
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /businesses/{businessUid}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == businessUid;
    }
  }
}
```

Esto garantiza aislamiento estricto por el identificador único `request.auth.uid` de Firebase Auth.