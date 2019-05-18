let coresMeias = [10, 20, 20, 10, 10, 30, 50, 10, 20];

function sockMerchant(n, ar) {
    coresMeias.sort();
    console.log(coresMeias);
    let cont = 0;
    for (let i = 0; i < ar.lenght; i++){
        if(ar.indexOf(ar[i] == ar[i + 1])){
            cont++;
            i++;
        }
    }
    return cont;
}


console.log(sockMerchant(10, coresMeias));
