# Dev Inventory

Aplicativo Android desenvolvido em **Kotlin** com **Jetpack Compose** e **Firebase Firestore** para gerenciamento de ferramentas utilizadas no desenvolvimento de software.

## Sobre o projeto

O **Dev Inventory** permite cadastrar e organizar ferramentas de desenvolvimento, armazenando suas informações no Firebase Firestore.

O projeto foi desenvolvido como atividade prática para trabalhar com:

* Desenvolvimento Android
* Interface com Jetpack Compose
* Operações CRUD
* Integração com Firebase
* Banco de dados em nuvem

## Funcionalidades

### Cadastro

Permite cadastrar novas ferramentas informando:

* Nome
* Tipo
* Categoria
* Versão
* Licença
* Plataforma
* Status
* Observações

### Listagem

As ferramentas cadastradas são carregadas automaticamente do Firestore e exibidas na tela principal.

### Pesquisa

É possível pesquisar ferramentas pelo:

* Nome
* Tipo
* Categoria

### Edição

Uma ferramenta existente pode ser editada através do botão de edição.

### Exclusão

É possível excluir uma ferramenta do Firestore através do botão de exclusão, com confirmação antes da remoção.

### Exemplos

O aplicativo possui uma opção para carregar ferramentas de exemplo diretamente no Firestore.

## Tecnologias utilizadas

* **Kotlin**
* **Android Studio**
* **Jetpack Compose**
* **Material 3**
* **Firebase Firestore**
* **Gradle**

## Estrutura principal

```text
DevInventory/
├── app/
│   ├── src/
│   │   └── main/
│   │       └── java/
│   │           └── com/
│   │               └── devinventory/
│   │                   └── app/
│   │                       └── MainActivity.kt
│   ├── build.gradle.kts
│   └── google-services.json
│
├── gradle/
│   └── wrapper/
│
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
└── gradlew.bat
```

## Firebase

O aplicativo utiliza o **Cloud Firestore** para armazenar as ferramentas.

Os dados são organizados na coleção:

```text
ferramentas
```

Cada documento contém informações como:

```text
name
type
category
version
license
platform
status
notes
```

## Como executar

1. Clone o repositório:

```bash
git clone 
```

2. Abra o projeto no **Android Studio**.

3. Aguarde a sincronização do Gradle.

4. Configure o Firebase utilizando o arquivo:

```text
app/google-services.json
```

5. Execute o aplicativo em um dispositivo Android ou emulador.

## Observação

Este projeto foi desenvolvido para fins acadêmicos e de aprendizado, com foco na utilização do Firebase Firestore em uma aplicação Android.


## Vídeo no drive 

https://drive.google.com/file/d/1oR0P-e218nTufnabF6A8KkT_NZNeeyC-/view?usp=drivesdk
