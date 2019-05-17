function sockMerchant(n, ar) {
    let sorted = ar.sort( (a,b) => a - b);
    let pares = 0;

    for(let i = 0; i < n; i++){
        if(sorted[i] === sorted [i + 1]){
            pares++;
            i += 1;
        }
    }

    return pares;
}


console.log(sockMerchant(9, [10, 20, 20, 10, 10, 30, 50, 10, 20]))

