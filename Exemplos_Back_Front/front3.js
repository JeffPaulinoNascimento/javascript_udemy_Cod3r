(function($) {

    $().ready(function() {
        $.paramsPage = {
            contexto: "/plantao/apontamentoHoras/",
        };
    
        $('.hora').timepicker({ 'timeFormat': 'H:i', 'step':15});
        $(".calendario").datepicker({dateFormat: 'dd/mm/yy',changeMonth: true,changeYear: true});
        $(".calendario").mask("99/99/9999");
        
        $(".inicioPlantao").change(function() {
            $("#alteracaoInicio").val(true);
        });
        
        $("#horaEntrada").change(function() {
            $("#alteracaoInicio").val(true);
        });
        
        $(".fimPlantao").change(function() {
            $("#alteracaoFim").val(true);
        });
        
        $("#horaSaida").change(function() {
            $("#alteracaoFim").val(true);
        });
        
        $(".dataInicioPausa").change(function() {
            $(this).parent().find(".alteracaoInicioPausa").val(true);
        });
        
        $(".horaInicioPausa").change(function() {
            $(this).parent().parent().prev().find(".alteracaoInicioPausa").val(true);
        });
        
        $(".dataTerminoPausa").change(function() {
            $(this).parent().find(".alteracaoFimPausa").val(true);
        });
        
        $(".horaTerminoPausa").change(function() {
            $(this).parent().parent().prev().find(".alteracaoFimPausa").val(true);
        });	
        
            
        $(".adicionarPausa").click(function() {
            $("input").removeClass("error");
            $("#lista-pausas fieldset").removeClass("error");
            
            var dataEntrada = moment($("#dataEntrada").val() + " " + $("#horaEntrada").val(), "DD/MM/YYYY HH:mm", true);
            
            if(!dataEntrada.isValid()) {
                $.gerarNotificacao("topCenter","warning",true,"aviso","Preencha a data de entrada",true);
                $("#dataEntrada,#horaEntrada").addClass("error");
                return;
            }
            
            var erro = false;
    
            $("#lista-pausas fieldset").each(function(index,linha){
                var inicio = $(linha).find(".dataInicioPausa").val() + " " + $(linha).find(".horaInicioPausa").val();
                var fim = $(linha).find(".dataTerminoPausa").val() + " " + $(linha).find(".horaTerminoPausa").val();
                var pausaInicio = moment(inicio,"DD/MM/YYYY HH:mm", true);
                var pausaFim = moment(fim ,"DD/MM/YYYY HH:mm", true);
                if(!pausaInicio.isValid() || !pausaFim.isValid()) {
                    $(linha).addClass("error");
                    erro = true;
                }
            });
            
            if(erro){
                $.gerarNotificacao("topCenter","warning",true,"aviso","Há uma pausa em andamento",true);
                return;
            }
    
            var clone = $("#templatePausa").clone(true,true).removeAttr("id");
            clone.find("input").removeClass('hasDatepicker').removeAttr('id');
                    
            $("#lista-pausas").append(clone);
            
            clone.find(".calendario").datepicker({dateFormat: 'dd/mm/yy',changeMonth: true,changeYear: true});
            clone.find(".calendario").mask("99/99/9999");
            clone.find(".hora").timepicker({ 'timeFormat': 'H:i', 'step':15});
            $.atualizarIframe();
        });
        
        $(".deletar-pausa").on("click", function() {
            if (confirm("Confirma remoção da pausa?")) {
                $(this).parent().parent().parent().remove();
                if($("#lista-pausas fieldset").length == 0){
                    $(".inicioPlantao").prop("disabled",false);	
                }
                $.atualizarIframe();
            }
        });
        
        $("#ausente").on("click", function() {
            if (confirm("Confirma ausência do profissional?")) {
                $.post($.paramsPage.contexto + "apontar/marcarComoAusente",{idPlantao : $("#idPlantao").val(),observacao: $("#observacao").val()}).done(function(data) {
                    parent.$.buscarPlantoes();
                    parent.$.fancybox.close();
                });	
            }
        });
        
        $("#remover").on("click", function(){
            if (confirm("Confirma a remoção do apontamento?")) {
                $.post($.paramsPage.contexto + "removerApontamento",{idPlantao : $("#idPlantao").val()}).done(function(data) {
                    parent.$.buscarPlantoes();
                    parent.$.fancybox.close();
                });	
            }
        });
            
        $(".inicioPlantao").change(function() {
            var preenchido = true;
            $(".inicioPlantao").each(function(index,campo){
                if($(campo).val()== ""){
                    preenchido = false;
                }
            });
            if(preenchido){
                $(".fimPlantao").prop("disabled",false);
            }
            else{
                $(".fimPlantao").prop("disabled",true);
                $(".fimPlantao").val("");
            }
        });
        
        
        
        $("#salvar").click(function() {
            $.noty.closeAll();
            
            var dataEntrada = moment($("#dataEntrada").val() + " " + $("#horaEntrada").val(), "DD/MM/YYYY HH:mm", true);
            
            if(!dataEntrada.isValid()) {
                $("#dataEntrada").addClass("error");
                $("#horaEntrada").addClass("error");
                $.gerarNotificacao("topCenter","error",true,"aviso","Preencha a data de entrada",true);
                return;
            }
            
            $("#dataEntrada").removeClass("error");
            $("#horaEntrada").removeClass("error");
            $("input").removeClass("error");
            $("#lista-pausas fieldset").removeClass("error");
            
            var dataEntrada = null;
            var dataSaida = null;
            var stDataEntrada = "";
            var stDataSaida = "";
            
            if($("#dataEntrada").val() != "" && $("#horaEntrada").val() != ""){
                dataEntrada = moment($("#dataEntrada").val() + " " + $("#horaEntrada").val(), "DD/MM/YYYY HH:mm");
                stDataEntrada = $("#dataEntrada").val() + " " + $("#horaEntrada").val();
            }
            
            if($("#dataSaida").val() != "" && $("#horaSaida").val() != ""){
                dataSaida = moment($("#dataSaida").val() + " " + $("#horaSaida").val(), "DD/MM/YYYY HH:mm");
                stDataSaida = $("#dataSaida").val() + " " + $("#horaSaida").val();
            }
    
            if(($("#dataEntrada").val() != "" && $("#dataEntrada").val() != $("#dataInicioPlanejado").val()) || ($("#dataSaida").val() != "" && $("#dataSaida").val() != $("#dataFimPlanejado").val())) {
                if(!confirm("A data de execução do plantão não é igual a data planejada!\nConfirma esta data de execução do plantão?")) {
                    return;
                }
            }
            
            if(dataEntrada != null && dataSaida != null){
                if(dataSaida.isBefore(dataEntrada)) {
                    $.gerarNotificacao("topCenter","warning",true,"Aviso","A data de saída é anterior a data de entrada",true);
                    $("#dataSaida").addClass("error");
                    $("#horaSaida").addClass("error");
                    return;
                }
    
                var duration = moment.duration(dataSaida.diff(dataEntrada));
                var hours = duration.asHours();
    
                if(hours > 24 ){
                    $.gerarNotificacao("topCenter","warning",true,"Aviso","O plantão não pode ultrapassar 24 Horas",true);
                    return;
                }
            }
            
            var erros = [];
            var dataInicio = moment($("#dataEntrada").val() + " " + $("#horaEntrada").val(), "DD/MM/YYYY HH:mm");
            var dataFim = null;
            if($("#dataSaida").val() != "" && $("#horaSaida").val() != ""){
                dataFim = moment($("#dataSaida").val() + " " + $("#horaSaida").val(), "DD/MM/YYYY HH:mm");
            }
            
            $("#lista-pausas fieldset").each(function(index,linha){
                
                var entradaOK = false;
                var saidaOK = false;
                
                if(index == 0){
                    var dataInicioPausa = moment($(linha).find(".dataInicioPausa").val() + " " + $(linha).find(".horaInicioPausa").val(), "DD/MM/YYYY HH:mm");					
                    var dataFimPausa = null;
                    
                    if($(linha).find(".dataTerminoPausa").val() != "" && $(linha).find(".horaTerminoPausa").val() != ""){
                        dataFimPausa = moment($(linha).find(".dataTerminoPausa").val() + " " + $(linha).find(".horaTerminoPausa").val(), "DD/MM/YYYY HH:mm");
                    }
                    
                    if(
                        (dataInicio.isValid() && dataInicioPausa.isValid() && dataInicioPausa.isAfter(dataInicio)) && 
                        (dataFim != null && dataFim.isValid() && dataInicioPausa.isBefore(dataFim))
                    ){
                        entradaOK = true;
                    }
                    else if((dataInicio.isValid() && dataInicioPausa.isValid() && dataInicioPausa.isAfter(dataInicio)) && dataFim == null){
                        entradaOK = true;
                    }
                                    
                    if(dataFim == null){						
                        if(dataFimPausa != null){
                            if(dataFimPausa.isValid() && dataFimPausa.isAfter(dataInicioPausa)){
                                saidaOK = true;
                            }
                            else{
                                saidaOK = false;
                            }							
                            if(dataFim != null){
                                if(dataFim.isValid() && dataFimPausa.isBefore(dataFim)){
                                    saidaOK = true;
                                }
                                else{
                                    saidaOK = false;
                                }							
                            }
                        }
                        else{
                            saidaOK = true;
                        }
                    }
                    else{
                        if(dataFimPausa != null){
                            if(dataFimPausa.isValid() && dataFimPausa.isAfter(dataInicioPausa)){
                                saidaOK = true;
                            }
                            else{
                                saidaOK = false;
                            }							
                            if(dataFim != null){
                                if(dataFim.isValid() && dataFimPausa.isBefore(dataFim)){
                                    saidaOK = true;
                                }
                                else{
                                    saidaOK = false;
                                }							
                            }
                        }
                        else{
                            saidaOK = false;
                        }						
                    }
                    
                    
                    if(!entradaOK || !saidaOK){
                        erros.push(linha);	
                    }
                }
                
                if(index != 0){
                    var dataInicioPausa = moment($(linha).find(".dataInicioPausa").val() + " " + $(linha).find(".horaInicioPausa").val(), "DD/MM/YYYY HH:mm");					
                    var dataFimPausa = null;
                    
                    if($(linha).find(".dataTerminoPausa").val() != "" && $(linha).find(".horaTerminoPausa").val() != ""){
                        dataFimPausa = moment($(linha).find(".dataTerminoPausa").val() + " " + $(linha).find(".horaTerminoPausa").val(), "DD/MM/YYYY HH:mm");
                    }
                    
                    var linhaAnterior = $("#lista-pausas fieldset").eq(index-1);
                    
                    var dataInicioAnterior = moment($(linhaAnterior).find(".dataInicioPausa").val() + " " + $(linhaAnterior).find(".horaInicioPausa").val(), "DD/MM/YYYY HH:mm");
                    var dataFimAnterior = moment($(linhaAnterior).find(".dataTerminoPausa").val() + " " + $(linhaAnterior).find(".horaTerminoPausa").val(), "DD/MM/YYYY HH:mm");
                    
                    if(dataInicioPausa.isValid() && dataInicioPausa.isAfter(dataFimAnterior)){
                        entradaOK = true;
                    }
                    else{
                        entradaOK = false;
                    }
                    
                    if(dataFim != null){
                        if(dataFim.isValid() && dataInicioPausa.isBefore(dataFim)){
                            entradaOK = true;
                        }
                        else{
                            entradaOK = false;
                        }
                    }
                    
                    if(dataFimPausa != null){
                        if(dataFimPausa.isValid() && dataFimPausa.isAfter(dataInicioPausa)){
                            saidaOK = true;
                        }
                        else{
                            saidaOK = false;
                        }							
                        if(dataFim != null){
                            if(dataFim.isValid() && dataFimPausa.isBefore(dataFim)){
                                saidaOK = true;
                            }
                            else{
                                saidaOK = false;
                            }							
                        }
                    }
                    
                    if(!entradaOK || !saidaOK){
                        erros.push(linha);	
                    }					
                    
                }
            });
            
            $("#lista-pausas fieldset").removeClass("error");
                    
            if(erros.length > 0){
                $(erros).each(function(index,linha){
                    $(linha).addClass("error");
                });
                $.gerarNotificacao("topCenter","warning",true,"Aviso", erros.length + " pausa(s) preenchida(s) incorretamente",true);
                return;
            }
    
            var pausas = [];
            
            $("#lista-pausas fieldset").each(function(index,linha){
                var inicio = $(linha).find(".dataInicioPausa").val() + " " + $(linha).find(".horaInicioPausa").val();
                var fim = $(linha).find(".dataTerminoPausa").val() + " " + $(linha).find(".horaTerminoPausa").val();
                var pausa = {
                        dataInicio				:	inicio,
                        dataFim					:	fim,
                        alteracaoEntrada		:	$(linha).find(".alteracaoInicioPausa").val(), 
                        apontadorEntrada		:	($(linha).find(".pausaApontadorEntrada").val() == "" ? null : $(linha).find(".pausaApontadorEntrada").val()),
                        alteracaoSaida			:	$(linha).find(".alteracaoFimPausa").val(), 
                        apontadorSaida			:	($(linha).find(".pausaApontadorSaida").val() == "" ? null : $(linha).find(".pausaApontadorSaida").val())
                }
                pausas.push(pausa);
            });		
            
            var envio = {
                    idPlantao			: $("#idPlantao").val(),
                    entrada				: stDataEntrada.trim() != "" ? stDataEntrada : null,
                    saida				: stDataSaida.trim() != "" ?  stDataSaida : null,
                    alteracaoInicio		: $("#alteracaoInicio").val(),		
                    alteracaoFim		: $("#alteracaoFim").val(),
                    apontadorEntrada	: ($("#apontadorEntrada").val() == "" ? null : $("#apontadorEntrada").val()),		
                    apontadorSaida		: ($("#apontadorSaida").val() == "" ? null : $("#apontadorSaida").val()),
                    observacao			: $("#observacao").val(),
                    pausas				: pausas
            };
            
            
    
            $.ajax({
                type: "POST",
                url: $.paramsPage.contexto + "apontar/salvar",
                data: JSON.stringify(envio),
                success: function(data) {
                    parent.$.atualizarPlantao(data);
                    parent.$.fancybox.close();
                },
                error: function() {
                    $.gerarNotificacao("bottomFooter","error",false,"Atenção","Erro ao salvar");
                },
                async: true,
                dataType: "json",  
                contentType: "application/json;"
            });
            
            
        });
        
        
        $.extend({
            atualizarIframe: function(){
                try {				
                    parent.$.fancybox.update();	
                } catch (e) {}
            }
        });
        $.disableButtons(false);
    });
    
    })(jQuery);