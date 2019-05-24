(function ($) {

    $().ready(function () {
        $.paramsPage = {
            contexto: "/plantao/agendamento/calendario/",
            profissionais: null,
            subEspecialidades: null,
            criadores: null,
            box: null
        };

        $(".remove-filters").click(function(){
            $("#redefinir").click();
        });

        $("#pesquisar").click(function(){
            $.paramsPage.profissionais = $("#profissionais")[0].selectize.getValue();
            $.paramsPage.subEspecialidades = $("#subespecialidade")[0].selectize.getValue();
            $.paramsPage.criadores = $("#criadores")[0].selectize.getValue();
            var especialidade = $("#especialidade")[0].selectize.getValue();

            if(especialidade.length == 1 && $.paramsPage.subEspecialidades.length == 0){
                $.gerarNotificacao("topCenter", "information", true, "Informação", "Selecione uma subespecialidade",true);
                return;
            }


            if ((typeof $.paramsPage.profissionais !== 'undefined' && $.paramsPage.profissionais.length > 0) ||
                (typeof $.paramsPage.subEspecialidades !== 'undefined' && $.paramsPage.subEspecialidades.length > 0) ||
                (typeof $.paramsPage.criadores !== 'undefined' && $.paramsPage.criadores.length > 0)
            ) {
                $(".header-filtros").removeClass("hide");
            }

            $('#calendario').fullCalendar('refetchEvents');
            $.fancybox.close();

        });

        $("#redefinir").click(function(){
            $("#profissionais")[0].selectize.clear();
            $("#profissionais")[0].selectize.clearOptions();

            $("#especialidade")[0].selectize.clear();

            $("#subespecialidade")[0].selectize.clear();
            $("#subespecialidade")[0].selectize.clearOptions();

            $("#criadores")[0].selectize.clear();
            $("#criadores")[0].selectize.clearOptions();

            $.paramsPage.profissionais = null;
            $.paramsPage.subEspecialidades = null;
            $.paramsPage.criadores = null;

            $(".header-filtros").addClass("hide");

            $('#calendario').fullCalendar('refetchEvents');

            $.fancybox.close();

        });

        $('#profissionais').selectize({
            valueField: 'idProfissional',
            labelField: 'nomeProfissional',
            searchField: ['nomeProfissional'],
            sortField: 'nomeProfissional',
            create: false,
            maxItems: null,
            onChange: function (value) {
                $.fancybox.update();
            },
            render: {
                item: function (item, escape) {
                    return '<div>' + escape(item.nomeProfissional) + '</div>';
                },
                option: function (item, escape) {
                    var retorno = "";
                    retorno += '<div>';
                    retorno += '<div class="selectize ' + ((item.residente + "" == "true") ? "nomeResidente" : "nomeProfissional") + '">' + escape(item.nomeProfissional) + '</div>';
                    retorno += '</div>';
                    return retorno;
                },
            },
            load: function (query, callback) {
                if (!query.length) return callback();
                $.ajax({
                    url: $.paramsPage.contexto + "buscarProfissional",
                    type: 'post',
                    dataType: 'json',
                    data: {
                        consulta: query,
                        idLocal: $("#idLocal").val()
                    },
                    error: function () {
                        callback();
                    },
                    success: function (res) {
                        callback(res);
                    }
                });
            }
        });

        $('#especialidade').selectize({
            maxItems: 1,
            create: false,
            onChange: function (value) {

                $.post($.paramsPage.contexto + "buscarEspecialidades", {
                    idEspecialidade: value,
                })
                    .done(function (retorno) {
                        $("#subespecialidade")[0].selectize.clear();
                        $("#subespecialidade")[0].selectize.clearOptions();

                        $(retorno).each(function (index, opcao) {

                            $("#subespecialidade")[0].selectize.addOption({"id": opcao.id, "nome": opcao.nome});

                        });

                        if(value == ""){
                            $(".label-obrigatorio").removeClass("obrigatorio");
                        }
                        else{
                            $(".label-obrigatorio").addClass("obrigatorio");
                        }

                    })
                    .fail(function () {
                        $("#subespecialidade")[0].selectize.clear();
                        $("#subespecialidade")[0].selectize.clearOptions();
                        $.gerarNotificacao("topCenter", "error", true, "erro", "Erro ao buscar as sub-especialidades");
                    });
            }
        });

        $('#subespecialidade').selectize({
            valueField: 'id',
            labelField: 'nome',
            searchField: ['nome'],
            sortField: 'nome',
            maxItems: null,
            create: false,
            onChange: function (value) {
                $.fancybox.update();
            }
        });

        $('#criadores').selectize({
            valueField: 'id',
            labelField: 'nome',
            searchField: ['nome'],
            sortField: 'nome',
            create: false,
            onlyTop: true,
            maxItems: null,
            onChange: function (value) {
                $.fancybox.update();
            },
            load: function(query, callback) {
                if (!query.length) return callback();
                $.ajax({
                    url: "/portalweb/usuario/pesquisarUsuario",
                    type: 'post',
                    dataType: 'json',
                    data: {
                        nome:query,
                    },
                    error: function() {
                        callback();
                    },
                    success: function(res) {
                        callback(res);
                    }
                });
            },
            render: {
                item: function(item, escape) {
                    var txt = '<div class="item-usuario">' + escape(item.nome) +  '</div>';
                    txt += '</div>';
                    return txt;
                }
            }
        });

        $('#calendario').fullCalendar({
            axisFormat: 'HH:mm',
            selectable: true,
            allDaySlot: false,
            timeZone: 'America/Sao_Paulo',
            select: function (start, end) {
                var agora = moment();

                if ($('#calendario').fullCalendar('getView').name != "month") {
                    if (agora.subtract(15, 'minutes').isAfter(moment(start.format()).toDate())) {
                        $.gerarNotificacao("topCenter", "information", true, "Informação", "Não é pertimido criar um plantão para uma dia/horário antigo", true);
                        $('#calendario').fullCalendar('unselect');
                        return;
                    }
                }
                else {
                    if (agora.subtract(1, 'days').isAfter(moment(start.format()).toDate())) {
                        $.gerarNotificacao("topCenter", "information", true, "Informação", "Não é pertimido criar um plantão para uma dia/horário antigo", true);
                        $('#calendario').fullCalendar('unselect');
                        return;
                    }
                }
                var evento = {
                    inicio: moment(start.format()).toDate().getTime(),
                    fim: moment(end.format()).toDate().getTime(),
                    idLocal: $("#idLocal").val()
                };
                $.cadastrarAgendamento(evento);
            },
            events: function (start, end, timezone, callback) {
                $.post($.paramsPage.contexto + "listarAgendamentos", {
                    start               : moment(start.format()).toDate().getTime(),
                    end                 : moment(end.format()).toDate().getTime(),
                    idLocal             : $("#idLocal").val(),
                    profissionais       : $.paramsPage.profissionais,
                    subespecialidades   : $.paramsPage.subEspecialidades,
                    criadores           : $.paramsPage.criadores
                })
                    .done(function (retorno) {
                        callback(retorno);
                    })
                    .fail(function () {
                        $.gerarNotificacao("topCenter", "error", true, "erro", "Erro ao buscar os agendamentos");
                    });
            },
            eventAfterRender: function (event, element) {

                element.attr({"title": "Profissional: " + event.nomeProfissional + " \nTipo de plantão: " + event.tipoPlantao});

                if (event.ativo) {
                    element.find('.fc-title').prepend("<i class='fa fa-eye-slash' ></i> ");
                }

                if (event.tipoAtendimentoIcone != "") {
                    element.find('.fc-title').prepend("<i class='fa " + event.tipoAtendimentoIcone + "'></i> ");
                }

                if (event.marcacaoIcone !== undefined) {
                    element.find('.fc-title').prepend("<i class='fa "+ event.marcacaoIcone +"' ></i> ");
                }

                //alert($('#calendario').fullCalendar('getView').name);


                /*
                if($('#calendario').fullCalendar('getView').name != "month" ){
                    element.find('.fc-title').append("<b> (" + event.nomeProfissional + ")</b>");
                }*/
            },
            header: {
                left: 'prev,next today filtroCalendario',
                center: 'title',
                right: 'agendaDay,agendaWeek,month,listMonth'
            },
            editable: true,
            navLinks: true, // can click day/week names to navigate views
            locale: 'pt-br',
            views: {
                month: {timeFormat: 'HH:mm', displayEventEnd: true},
            },
            eventDrop: function (event, delta, revertFunc, jsEvent, ui, view) {
                $.validarPeriodo(event, revertFunc);
            },
            customButtons: {
                filtroCalendario: {
                    text: 'Filtrar eventos',
                    click: function() {
                        $.openModalFancyInline("filtros-area");
                    }
                }
            },
            eventConstraint: {
                start: moment().format('YYYY-MM-DD'),
                end: '2100-01-01'
            },
            eventResize: function (event, delta, revertFunc, jsEvent, ui, view) {
                $.validarPeriodo(event, revertFunc);
            },
            eventClick: function (calEvent, jsEvent, view) {
                if($("#modeloCriacaoPlantao").val() == 'MULTIPLO') {
                    $.openModalFancy($.paramsPage.contexto + "editarAgendamento/multiplo/" + calEvent.id, "iframe", "90%", "90%");
                } else {
                    $.openModalFancy($.paramsPage.contexto + "editarAgendamento/" + calEvent.id, "iframe", "750px", "auto");
                }
            },
            noEventsMessage: "Não há eventos para este mês"
        })

        $("#criar").click(function () {
            $.cadastrarAgendamento({
                idLocal: $("#idLocal").val()
            });
        });

        $("#prox").click(function () {
            $('#calendario').fullCalendar('next');
        });

        $("#back").click(function () {
            $('#calendario').fullCalendar('prev');
        });

        $.extend({
            getStyleRuleValue: function (style, selector, sheet) {
                var sheets = typeof sheet !== 'undefined' ? [sheet] : document.styleSheets;
                for (var i = 0, l = sheets.length; i < l; i++) {
                    var sheet = sheets[i];
                    if (!sheet.cssRules) {
                        continue;
                    }
                    for (var j = 0, k = sheet.cssRules.length; j < k; j++) {
                        var rule = sheet.cssRules[j];
                        if (rule.selectorText && rule.selectorText.split(',').indexOf(selector) !== -1) {
                            return rule.style[style];
                        }
                    }
                }
                return null;
            },
            validarPeriodo: function (event, revertFunc) {
                $.ajax({
                    type: "POST",
                    url: $.paramsPage.contexto + "checarAgendamentoNoPeriodo",
                    data: {
                        inicio: moment(event.start.format()).toDate().getTime(),
                        fim: moment(event.end.format()).toDate().getTime(),
                        idPlantao: event.id,
                        idProfissional: event.idProfissional,
                        idLocalAtendimento: $("#idLocal").val()
                    },
                    success: function (retorno) {
                        if (retorno.length == 0) {

                            $.post($.paramsPage.contexto + "alterarPeriodoAgendamento", {id: event.id, start: moment(event.start).format('L HH:mm'), end: moment(event.end).format('L HH:mm')})
                                .done(function (data) {
                                	if(data.length > 0) {
                                		$.recarregarEventos();
                                		$.gerarNotificacao("topCenter", "error", true, "Erro", data[0].valor);
                                	}
                                })
                                .fail(function () {
                                    $.gerarNotificacao("topCenter", "error", true, "Erro", "Erro ao alterar a data do evento");
                                });
                        }
                        else {
                            $(retorno).each(function (index, mensagem) {
                                $.addErrorToList(mensagem.valor);
                            });

                            $.gerarNotificacao("topCenter", "warning", true, "Aviso", $.convertErrorsToList());
                            revertFunc();
                        }
                    },
                    error: function () {
                        $.gerarNotificacao("topCenter", "error", true, "Erro", "Erro ao validar o periodo", true);
                    },
                    async: true,
                });
            },
            cadastrarAgendamento: function (data) {
            	if($("#modeloCriacaoPlantao").val() == 'MULTIPLO') {
            		$.openModalFancy($.paramsPage.contexto + "criarAgendamentoMultiplo?" + $.param(data), "iframe", "90%", "100%");
            	} else {
	        		$.openModalFancy($.paramsPage.contexto + "criarAgendamento?" + $.param(data), "iframe", "750px", "auto");
            	}

                $('#calendario').fullCalendar('unselect');
            },
            atualizarCalendario: function (avisos,mensagemSucesso,naoFecharCaixa) {
                var erros = false
                $(avisos).each(function (index, aviso) {
                    erros = true;
                    $.addErrorToList(aviso.valor);
                });
                if (erros) {
                    $.gerarNotificacao("topCenter", "warning", false, "Aviso", $.convertErrorsToList(), true);
                }

                if(mensagemSucesso !== undefined && mensagemSucesso != null){
                    $.gerarNotificacao("topCenter", "success", false, "Sucesso", mensagemSucesso, true);
                }

                $.recarregarEventos();

                if(naoFecharCaixa !== undefined){
                    return;
                }

                $.fancybox.close();
            },
            recarregarEventos: function () {
                $('#calendario').fullCalendar('refetchEvents');
            },
            removerEventoCalendario: function (id) {
                $('#calendario').fullCalendar('removeEvents', id);
                $.fancybox.close();
            },
            irParaDia: function (dia) {
                $.fancybox.close();
                $('#calendario').fullCalendar('changeView', "agendaDay");
                $('#calendario').fullCalendar('gotoDate', moment(parseInt(dia)));
            },
            openModalFancyInline: function(elemento){
                $.paramsPage.box = $.fancybox.open([{
                    href : "#"+elemento,
                    type:'inline',
                    afterClose : function() {
                    },
                    tpl: {
                        closeBtn: '<div title="Fechar" class="fancybox-item fancybox-close"></div>'
                    }}], {padding : 0});
            },
        });
    });

})(jQuery);