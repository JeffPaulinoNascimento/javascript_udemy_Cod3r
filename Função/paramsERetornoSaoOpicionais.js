function area(largura, altura){
    const area = largura * altura;
    if(area > 20){
        console.log(`Valor acima do permitido: ${area}m2.`);
    }else{
        return area;
    }
}

console.log(area(2, 2));
console.log(area(2)); //passando somente um valor para a função o outro valor sera undefined, sendo assim, o retorno será um NaN, not a number
console.log(area(2, 5, 20, 1, 5));
console.log(area(5, 5));