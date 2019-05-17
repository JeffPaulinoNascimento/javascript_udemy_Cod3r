
var run = false;
var savevalue=0.00000001;
$(document).ready(function() {
   reclc();
});

function numberdigit(cislo) {
    document.getElementById("number1").textContent = Math.floor((cislo / 100000) % 10);
    document.getElementById("number2").textContent = Math.floor((cislo / 10000) % 10);
    document.getElementById("number3").textContent = Math.floor((cislo / 1000) % 10);
    document.getElementById("number4").textContent = Math.floor((cislo / 100) % 10);
    document.getElementById("number5").textContent = Math.floor((cislo / 10) % 10);
    document.getElementById("number6").textContent = Math.floor((cislo / 1) % 10);
}

function doc_keyUp(e) {
//alert(e.keyCode);
    // this would test for whichever key is 40 and the ctrl key at the same time
    if (e.keyCode == 72) {
        // call your function to do the thing
        bet(1);
    }
    else if (e.keyCode == 76) {
        // call your function to do the thing
        bet(0);
    }
    else if (e.keyCode == 68) {
        // call your function to do the thing
        changevalue(0)
    }
    else if (e.keyCode == 67) {
        // call your function to do the thing
        changevalue(1)
    }
    else if (e.keyCode == 83) {
        // call your function to do the thing
        saveinput();
    }
    else if (e.keyCode == 70) {
        // call your function to do the thing
        loadinput();
    }
}
// register the handler
document.addEventListener('keyup', doc_keyUp, false);

function changevalue(plus) {
    if (plus == 1) {
        $('#betmoney').val(($('#betmoney').val() * 2).toFixed(8));
        reclc()
    }
    else {
        $('#betmoney').val(($('#betmoney').val() / 2).toFixed(8));
        reclc()
    }

}
function saveinput() {

    var value=parseFloat($('#betmoney').val());
    if(isNaN(value)){
        return;
    }
    savevalue=value;
}
function loadinput() {
    $('#betmoney').val(savevalue);

}

function changenumbekkkr() {
    if (run == true) {
        numberdigit(Math.floor((Math.random() * 100000)));
        setTimeout(changenumbekkkr, 20);
    }
}
function reclc() {
    //$('#betmoney').val(($('#betmoney').val()*1).toFixed(8));
    var dan = 0;
    var šance = document.getElementById("winchance").value;
    var castka = document.getElementById("betmoney").value;
    vysledek = castka * 100 / šance;
    vysledek = (vysledek - castka) / 100 * (100 - dan);
    document.getElementById("winmoney").textContent = "You can win " + vysledek.toFixed(8)+ " Litecoin when you bet high and get a number higher than "
        + Math.round(100000-(100000 / 100 * šance))+" or bet low and get a number lower than " +  Math.round(100000 / 100 * šance);
}

function bet(high) {
    if ($("#buttonbetlow").is(":disabled")) {
        return;
    }


    var šance = document.getElementById("winchance").value;
    šance=Math.round(šance);
    var castka = document.getElementById("betmoney").value;
    if(isNaN(parseFloat(šance))||isNaN(parseFloat(castka))){
        document.getElementById("winlose").textContent = "Variable are incorrect";
        document.getElementById("winlose").style.color = "red";
        return;
    }

    document.getElementById("buttonbetlow").disabled = true;
    document.getElementById("buttonbethigh").disabled = true;
    run = true;
    changenumbekkkr();

    sendrequest(castka,šance,high,1);
}
function sendrequest(cryptovalue,winchance,betlow,repeat){
    $.ajax({
        url: 'php/betnow.php',
        type: 'post',
        cache: false,
        datatype: "text",
        data: {"money": cryptovalue, "winchance": winchance, "sudalicha": betlow},
        success: function (response) {
            /*run = false;
            if (response.indexOf("error") !== -1) {
                var str = response;
                var res = str.split(",");
                document.getElementById("winlose").textContent = res[1];
                document.getElementById("winlose").style.color = "red";
            }
            else {
                var str = response;
                var res = str.split(",");
                var a = parseFloat(res[0]).toFixed(8);

                document.getElementById("money").textContent = (a) + "";
                document.getElementById("moneybasictest").textContent = a;
                if (res[1] >= 0) {
                    document.getElementById("winlose").textContent = "You won " + parseFloat(res[1]).toFixed(8) + " number was " + res[2];
                    document.getElementById("winlose").style.color = "green";
                }
                else {
                    document.getElementById("winlose").textContent = "You lost " + parseFloat(res[1] * -1).toFixed(8) + " number was " + res[2];
                    document.getElementById("winlose").style.color = "red";

                }
                numberdigit(res[2]);
            }
            document.getElementById("buttonbethigh").disabled = false;
            document.getElementById("buttonbetlow").disabled = false;*/

            run = false;
            var result = response;
            if(result.state&&result.winlost){
                numberdigit(result.number);
                $("#money").text(result.cryptovalue);
                $("#moneybasicmultiply").text(result.cryptovalue);
                $("#winlose").text(result.messege);
                $('#winlose').css('color', 'green');

            }
            else if(result.state&&!result.winlost){
                numberdigit(result.number);
                $("#money").text(result.cryptovalue);
                $("#moneybasicmultiply").text(result.cryptovalue);
                $("#winlose").text(result.messege);
                $('#winlose').css('color', 'red');
            }
            else{
                $("#winlose").text(result.messege);
                $('#winlose').css('color', 'red');
            }
            document.getElementById("buttonbethigh").disabled = false;
            document.getElementById("buttonbetlow").disabled = false;


        },
        error: function (error) {
            if(repeat>0){
                sendrequest(cryptovalue,winchance,betlow,repeat-1);
            }
            else{
                run = false;
                $("#winlose").text("Server not respond");
                $('#winlose').css('color', 'red');
                document.getElementById("buttonbethigh").disabled = false;
                document.getElementById("buttonbetlow").disabled = false;


            }
        },
        timeout: 1500

    });
}
>>>>>>> 3921acdafc381a3941c870fa42eb86009b8af479
