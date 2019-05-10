function getInteiroAleatorio(min, max){
    const valor = Math.random() * (max - min) + min;
    console.log(`O valor gerado foi $`)
    return Math.floor(valor);
}

let opcao = 0;

while (opcao != -1) {
    opcao = getInteiroAleatorio(-1, 10);
    console.log(`Opção escolhida foi ${opcao}.`);
}

console.log("Até a próxima!");