Projeto exemplo: sistema bancário Spring Boot com JWT, SQS e testes de integração (Testcontainers).

Conteúdo:
- src/main/java: código principal (models, repos, services, controllers, security)
- src/test/java: testes de integração com Testcontainers
- pom.xml: dependências

Para rodar os testes:
mvn -DskipTests=false test

Avisos:
- Atualize secrets e queue-url antes de rodar em produção.
