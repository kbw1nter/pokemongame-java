# Pokémon: Jogo de Tabuleiro em Java

<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/9/98/International_Pok%C3%A9mon_logo.svg/1200px-International_Pok%C3%A9mon_logo.svg.png" alt="Pokémon Logo" width="400"/>

## 📖 Descrição

Este projeto é uma implementação de um jogo de tabuleiro inspirado no universo Pokémon, desenvolvido em **Java** como requisito para a disciplina de **Programação Orientada a Objetos (2025/1)** da Universidade Federal de Pelotas.

O jogo consiste em um tabuleiro onde dois treinadores, o jogador e o computador, se enfrentam em batalhas, capturam Pokémons selvagens e competem para se tornar o Mestre Pokémon com a maior pontuação de experiência. O projeto foi desenvolvido com foco na aplicação de conceitos essenciais de POO e arquitetura de software.

## ✨ Funcionalidades Principais

*   **Interface Gráfica**: Interface interativa construída com **Java Swing**, apresentando um tabuleiro (grid), painéis de status e logs de eventos.
*   **Batalhas de Pokémon**: Sistema de batalhas em turnos, onde cada tipo de Pokémon possui habilidades e cálculos de dano únicos.
*   **Inteligência Artificial (IA)**: O oponente (computador) possui uma lógica de jogada que é executada de forma assíncrona usando **Java Threads**, simulando um "tempo de pensar" sem travar a interface do usuário.
*   **Captura de Pokémon**: Possibilidade de encontrar e capturar Pokémons selvagens espalhados pelo tabuleiro.
*   **Persistência de Dados**: Funcionalidade para **salvar e carregar** o progresso do jogo em arquivos, permitindo continuar a partida posteriormente.
*   **Sistema de Pontuação**: Acompanhamento da pontuação de experiência de cada treinador, que é atualizada a cada vitória.

## 🏛️ Arquitetura e Padrões de Projeto

Para garantir um código limpo, extensível e de fácil manutenção, foram aplicados os seguintes padrões de projeto:

1.  **Strategy**: Utilizado para encapsular os algoritmos de ataque. Cada tipo de Pokémon (`Água`, `Floresta`, `Terra`, `Elétrico`) possui uma estratégia de cálculo de dano diferente, permitindo que novos comportamentos de ataque sejam adicionados sem modificar as classes de Pokémon existentes.
2.  **Factory Method**: Centraliza a lógica de criação dos objetos Pokémon. Uma fábrica é responsável por instanciar o tipo correto de Pokémon, desacoplando o cliente da implementação concreta das classes.
3.  **Observer**: Empregado para manter a interface gráfica sincronizada com o estado do jogo. A UI "observa" o motor do jogo e é notificada automaticamente sempre que ocorrem mudanças (ex: perda de energia, atualização de pontuação), atualizando-se sem a necessidade de acoplamento direto.

## 🛠️ Tecnologias e Ferramentas

*   **Linguagem**: Java 11 (ou superior)
*   **Interface Gráfica**: Java Swing
*   **Concorrência**: Java Threads e ExecutorService
*   **IDE utilizada**: IntelliJ IDEA
