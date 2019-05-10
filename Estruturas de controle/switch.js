const imprimirResultado = function (nota){
    switch(Math.floor(nota)){
        case 10:
        case 9:
            console.log("Quando de honra");
            break;
        case 8: case 7:
            console.log("Aprovado");
        case 6: case 5: case 4:
            console.log("Recuperação");
            break;
        case 3: case 2: case 1: case 0:
            console.log("Reprovado otário");
            break;
        default:
            console.log("Nota invalida")
    }
}

imprimirResultado(10);
imprimirResultado(7);
imprimirResultado(5);
imprimirResultado(1);
imprimirResultado(12);