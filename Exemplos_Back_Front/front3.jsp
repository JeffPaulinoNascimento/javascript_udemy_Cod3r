<%@page contentType="text/html; charset=ISO-8859-1" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="ps" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
 
<html>
 
    <head>
        <title>Prevent Senior :: Apontamento de Horas para Plantão</title>
		<ps:css-default-import/><ps:css-skin-import/>
		<ps:jquery-default-import/>
		<ps:jquery-notification-import/>
		<ps:jquery-gtimePicker-import/>
		<ps:jquery-ui-import-no-timepicker/>
		<ps:masks/>

		<ps:import-css uri="/resources/css/apontamentoHoras.css"/>

		<ps:import-script relative="false" uri="/portalweb/resources/jquery/moment.min.js"/>
		<ps:import-script relative="false" uri="/portalweb/resources/jquery/moment-timezone-with-data.js"/>
		<ps:import-script relative="false" uri="/portalweb/resources/js/contextActions.js"/>
		<ps:import-script uri="/resources/js/apontamentoHorasPlantao/apontamentoHora.js"/>
    </head>
 
  <body class="modal">
    	<div class="modal-area">
	    	<header class="modal template">
	    		<label>Apontamento de horas</label>
	    	</header>
    	</div>
    	<div class="modal-content">
	    	<form id="form" method="post">
	    		<ps:notification-fields/>
	    		<input type="hidden" name="ticket" value="${usuarioTicket.ticket}">
				<input type="hidden" id="idPlantao" value="${idPlantao}">
				<input type="hidden" name="plantaoRealizado.id" value="${plantao.plantaoRealizado.id}">
	    		<ps:form-fieldset-modal legend="Horário Planejado">
			    	<ps:form-container>
			    		<ps:form-column>
			    			<ps:form-field>
			    				<h2 class="modal"><i class="fa fa-hospital-o"></i>Local</h2>

								<c:set value="" var="divisaoOperacional"/>
								<c:if test="${plantao.localAtendimento.divisaoOperacional != null}">
									<c:set value="- (${plantao.localAtendimento.divisaoOperacional.nome})" var="divisaoOperacional"/>
								</c:if>

			    				<input type="text" disabled="disabled" value="${plantao.localAtendimento.localAtendimento.documento == plantao.localAtendimento.prestadorResponsavel.cnpj ? plantao.localAtendimento.localAtendimento.apelido : plantao.localAtendimento.prestadorResponsavel.apelido} ${divisaoOperacional}" class="modal"/>
			    			</ps:form-field>
			    		</ps:form-column>
		    		</ps:form-container>	    	
					<ps:form-container>	    		
			    		<ps:form-column>
			    			<c:if test="${plantao.nome != null}">
				    			<ps:form-field>
				    				<h2 class="modal"><i class="fa fa-clock-o"></i>Plantão</h2>
				    				<input disabled="disabled" value="${plantao.nome}" type="text" class="modal"/>
				    			</ps:form-field>
			    			</c:if>
			    			<ps:form-field>
			    				<h2 class="modal"><i class="fa fa-user"></i>Profissional</h2>
			    				<input disabled="disabled" value="${plantao.profissional.nome}" type="text" class="modal"/>
			    			</ps:form-field>
			    		</ps:form-column>
			    		<ps:form-column-custom id="valorHora">
			   				<c:if test="${plantao.nome != null}">
				    			<ps:form-field>
									<h2 class="modal"><i class="fa fa-coffee"></i>Intervalo</h2>
									<input disabled="disabled" value="${plantao.intervalo} minutos" type="text" class="modal"/>	    				
				    			</ps:form-field>
			    			</c:if>		    			
			    			<ps:form-field>
								<h2 class="modal"><i class="fa fa-pw-cracha"></i>CRM</h2>
								<input disabled="disabled" value="${plantao.profissional.conselho.numero}" type="text" class="modal"/>
			    			</ps:form-field>
			    		</ps:form-column-custom>
			    	</ps:form-container>
			    	<ps:form-container>
			    		<ps:form-column-custom extraClasses="periodoBloco">
			    			<ps:form-field extraClasses="periodo">
			    				<h2 class="modal">Começa em</h2>
			    				<ps:form-column-custom extraClasses="dataInput">
			    					<ps:form-field>
			    						<input id="dataInicioPlanejado" type="text" disabled="disabled" value="<fmt:formatDate pattern="dd/MM/yyyy" value="${plantao.dataInicio}" />" class="center modal"/>
			    					</ps:form-field>
			    				</ps:form-column-custom>
			    				<ps:form-column-custom extraClasses="horaInput">
			    					<ps:form-field>
			    						<input type="text" disabled="disabled" value="<fmt:formatDate pattern="HH:mm" value="${plantao.dataInicio}" />" class="center modal"/>
			    					</ps:form-field>
			    				</ps:form-column-custom>
			    			</ps:form-field>	    		
			    		</ps:form-column-custom>
			    		<ps:form-column-custom extraClasses="periodoBloco">
			    			<ps:form-field extraClasses="periodo">
			    				<h2 class="modal">Finaliza em</h2>
			    				<ps:form-column-custom extraClasses="dataInput">
			    					<ps:form-field>
			    						<input id="dataFimPlanejado" type="text" disabled="disabled" value="<fmt:formatDate pattern="dd/MM/yyyy" value="${plantao.dataFim}" />" class="center modal"/>
			    					</ps:form-field>
			    				</ps:form-column-custom>
			    				<ps:form-column-custom extraClasses="horaInput">
			    					<ps:form-field>
			    						<input type="text" disabled="disabled" value="<fmt:formatDate pattern="HH:mm" value="${plantao.dataFim}" />" class="center modal"/>
			    					</ps:form-field>
			    				</ps:form-column-custom>
			    			</ps:form-field>	    		
			    		</ps:form-column-custom>
		    		</ps:form-container>
				</ps:form-fieldset-modal>
				<c:if test="${plantao.plantaoRealizado.status == 'AUSENTE'}">
					<ps:form-fieldset-modal legend="Aviso">
						<div class="aviso-ausencia">Este profissional foi apontado como ausente por ${plantao.plantaoRealizado.apontadorEntrada.nome}</div>
					</ps:form-fieldset-modal>
				</c:if>
								
				<ps:form-fieldset-modal legend="Horário Executado">
			    	<ps:form-container>
			    		<ps:form-column-custom extraClasses="periodoBloco">
			    			<ps:form-field extraClasses="periodo">
			    				<h2 title="Entrada apontado por: ${plantao.plantaoRealizado.apontadorEntrada != null ? plantao.plantaoRealizado.apontadorEntrada.nome.concat(' em') : 'Não apontado'} <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${plantao.plantaoRealizado.dataInicioApontamento}"/>" class="modal obrigatorio"><i class="fa fa fa-arrow-circle-right"></i>Entrada</h2>
			    				<ps:form-column-custom extraClasses="dataInput">
			    					<ps:form-field>
										<input type="hidden" id="alteracaoInicio" name="alteracaoInicio" value="false"/>
										<input type="hidden" id="apontadorEntrada" value="${plantao.plantaoRealizado.apontadorEntrada.id}"/>
			    						<input type="text" id="dataEntrada" class="center modal datepicker inicioPlantao" value="<fmt:formatDate pattern="dd/MM/yyyy" value="${plantao.plantaoRealizado != null ? plantao.plantaoRealizado.dataInicio : ''}" />"/>
			    					</ps:form-field>
			    				</ps:form-column-custom>
			    				<ps:form-column-custom extraClasses="horaInput">
			    					<ps:form-field>
			    						<input type="text" class="center modal hora inicioPlantao" id="horaEntrada" value="<fmt:formatDate pattern="HH:mm" value="${plantao.plantaoRealizado != null ? plantao.plantaoRealizado.dataInicio : ''}" />"/>
			    					</ps:form-field>
			    				</ps:form-column-custom>
			    			</ps:form-field>	    		
			    		</ps:form-column-custom>
			    		<ps:form-column-custom extraClasses="periodoBloco">
			    			<ps:form-field extraClasses="periodo">
			    				<h2 title="Saída apontado por: ${plantao.plantaoRealizado.apontadorSaida != null ? plantao.plantaoRealizado.apontadorSaida.nome.concat(' em') : 'Não apontado'} <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${plantao.plantaoRealizado.dataFimApontamento}"/>" class="modal"><i class="fa fa fa-arrow-circle-left"></i>Saída</h2>
			    				<ps:form-column-custom extraClasses="dataInput">
			    					<ps:form-field>
			    						<input type="hidden" id="alteracaoFim" name="alteracaoFim" value="false"/>
			    						<input type="hidden" id="apontadorSaida" value="${plantao.plantaoRealizado.apontadorSaida.id}"/>
			    						<input type="text" id="dataSaida" ${plantao.plantaoRealizado.dataInicio == null ? 'disabled="disabled"' : ''} class="center modal datepicker fimPlantao" value="<fmt:formatDate pattern="dd/MM/yyyy" value="${plantao.plantaoRealizado != null ? plantao.plantaoRealizado.dataFim : ''}" />"/>
			    					</ps:form-field>
			    				</ps:form-column-custom>
			    				<ps:form-column-custom extraClasses="horaInput">
			    					<ps:form-field>
			    						<input type="text" id="horaSaida" ${plantao.plantaoRealizado.dataInicio == null ? 'disabled="disabled"' : ''} class="center modal hora fimPlantao" value="<fmt:formatDate pattern="HH:mm" value="${plantao.plantaoRealizado != null ? plantao.plantaoRealizado.dataFim : ''}" />"/>
			    					</ps:form-field>
			    				</ps:form-column-custom>
			    			</ps:form-field>	    		
			    		</ps:form-column-custom>
		    		</ps:form-container>				
				</ps:form-fieldset-modal>
				<ps:form-fieldset-modal legend="Observação">
					<ps:form-container>
						<ps:form-column>
							<ps:form-field>
								<textarea id="observacao" class="modal" maxlength="255" rows="" cols="">${plantao.plantaoRealizado.observacao}</textarea>
							</ps:form-field>
						</ps:form-column>
					</ps:form-container>
				</ps:form-fieldset-modal>				
				<ps:form-fieldset-modal legend="Pausas">
					<div class="pausa-area-button">
						<button type="button" id="adicionarPausa" class="adicionarPausa"><i class="fa fa-plus"></i>Adicionar pausa</button>
					</div>
					<ps:form-container extraClasses="lista-pausas" id="lista-pausas">
						<c:forEach items="${plantao.plantaoRealizado.pausas}" var="pausa">
							<ps:form-fieldset-modal>
								<ps:form-container>
									<ps:form-column-custom extraClasses="periodoBloco">
										<ps:form-field extraClasses="periodo">
										<h2 title="Início apontado por: ${pausa.apontadorEntrada != null ? pausa.apontadorEntrada.nome : 'Não apontado'}" class="modal obrigatorio"><i class="fa fa-play-circle"></i>Inicio</h2>
											<ps:form-column-custom extraClasses="dataInput">
						    					<ps:form-field>
						    						<input type="hidden" class="alteracaoInicioPausa" value="false"/>
						    						<input type="hidden" class="pausaApontadorEntrada" value="${pausa.apontadorEntrada.id}"/>
						    						<input type="text" class="dataInicioPausa center modal calendario" value="<fmt:formatDate pattern="dd/MM/yyyy" value="${pausa.dataInicio}" />"/>
						    					</ps:form-field>
						    				</ps:form-column-custom>
											<ps:form-column-custom extraClasses="horaInput">
												<ps:form-field>			
								    				<input type="text" class="horaInicioPausa center modal hora" value="<fmt:formatDate pattern="HH:mm" value="${pausa.dataInicio}" />"/>
												</ps:form-field>
											</ps:form-column-custom>
										</ps:form-field>
									</ps:form-column-custom>	
									<ps:form-column-custom extraClasses="periodoBloco">
										<ps:form-field extraClasses="periodo">
											<h2 title="Fim apontado por: ${pausa.apontadorSaida != null ? pausa.apontadorSaida.nome : 'Não apontado'}" class="modal"><i class="fa fa-stop-circle"></i>Fim</h2>
											<ps:form-column-custom extraClasses="dataInput">
						    					<ps:form-field>
						    						<input type="hidden" class="alteracaoFimPausa" value="false"/>
						    						<input type="hidden" class="pausaApontadorSaida" value="${pausa.apontadorSaida.id}"/>
						    						<input type="text" class="dataTerminoPausa center modal calendario" value="<fmt:formatDate pattern="dd/MM/yyyy" value="${pausa.dataFim}" />"/>
						    					</ps:form-field>
						    				</ps:form-column-custom>
											<ps:form-column-custom extraClasses="horaInput">
												<ps:form-field>			
								    				<input type="text" class="horaTerminoPausa center modal hora" value="<fmt:formatDate pattern="HH:mm" value="${pausa.dataFim}" />"/>
												</ps:form-field>
											</ps:form-column-custom>
										</ps:form-field>
									</ps:form-column-custom>
									<ps:form-column extraClasses="acao-pausa">
										<button title="Remover pausa" class="deletar-pausa" type="button">
											<i class="fa fa-trash"></i>
										</button>
									</ps:form-column>
								</ps:form-container>
							</ps:form-fieldset-modal>						
						</c:forEach>
					</ps:form-container>
				</ps:form-fieldset-modal>
   			</form>
   		</div>
    	<footer class="modal">
    		<button type="button" id="salvar" class="default-action template">Salvar</button>
    		<button type="button" id="ausente" class="default-action red">Ausente</button>
    		<c:if test="${plantao.plantaoRealizado != null}">
    			<button type="button" id="remover" class="default-action red">Remover Apontamento</button>
   			</c:if>
    	</footer>
		<div class="hide">
			<ps:form-fieldset-modal id="templatePausa">
				<ps:form-container>
					<ps:form-column-custom extraClasses="periodoBloco">
						<ps:form-field extraClasses="periodo">
						<h2 class="modal obrigatorio"><i class="fa fa-play-circle"></i>Inicio</h2>
							<ps:form-column-custom extraClasses="dataInput">
		    					<ps:form-field>
		    						<input type="hidden" class="alteracaoInicioPausa" value="false"/>
									<input type="hidden" class="pausaApontadorEntrada" value=""/>
		    						<input type="text" class="dataInicioPausa center modal calendario" value=""/>
		    					</ps:form-field>
		    				</ps:form-column-custom>
							<ps:form-column-custom extraClasses="horaInput">
								<ps:form-field>			
				    				<input type="text" class="horaInicioPausa center modal hora"/>
								</ps:form-field>
							</ps:form-column-custom>
						</ps:form-field>
					</ps:form-column-custom>	
					<ps:form-column-custom extraClasses="periodoBloco">
						<ps:form-field extraClasses="periodo">
							<h2 class="modal"><i class="fa fa-stop-circle"></i>Fim</h2>
							<ps:form-column-custom extraClasses="dataInput">
		    					<ps:form-field>
		    						<input type="hidden" class="alteracaoFimPausa" value="false"/>
									<input type="hidden" class="pausaApontadorSaida" value=""/>
		    						<input type="text" class="dataTerminoPausa center modal calendario"/>
		    					</ps:form-field>
		    				</ps:form-column-custom>
							<ps:form-column-custom extraClasses="horaInput">
								<ps:form-field>			
				    				<input type="text" class="horaTerminoPausa center modal hora"/>
								</ps:form-field>
							</ps:form-column-custom>
						</ps:form-field>
					</ps:form-column-custom>
					<ps:form-column extraClasses="acao-pausa">
						<button title="Remover pausa" class="deletar-pausa" type="button">
							<i class="fa fa-trash"></i>
						</button>
					</ps:form-column>
				</ps:form-container>
			</ps:form-fieldset-modal>
		</div>
    </body>
</html>
