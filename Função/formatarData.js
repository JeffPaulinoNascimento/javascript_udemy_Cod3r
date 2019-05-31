function formataDataComsegundos(dataaFormata){

    let arrayDataHora = dataaFormata.split(" ");

    let dataquebrada = arrayDataHora[0].split("/");
    let horaquebrada = arrayDataHora[1].split(":");


    return new Date(dataquebrada[2], dataquebrada[1]-1, dataquebrada[0], horaquebrada[0], horaquebrada[1], horaquebrada[0]).getTime()

}