<%@page contentType="text/html; charset=ISO-8859-1" pageEncoding="UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="ps" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="plantao" tagdir="/WEB-INF/tags/plantao" %>
<!DOCTYPE html>
 
<html>
 
    <head>
        <title>Prevent Senior :: Lista de plantões (Calendário)</title>
		<ps:css-default-import/>
		<ps:css-skin-import/>		
		<ps:jquery-default-import/>
		<ps:jquery-datatable-import/>
  		<ps:jquery-notification-import/>
  		<ps:jquery-fancybox-import/>
		<ps:jquery-tags-selectize-import/>

		<script type="text/javascript" charset="utf-8" src="/portalweb/resources/jquery/moment.min.js"></script>
		<script type="text/javascript" charset="utf-8" src="/portalweb/resources/jquery/moment-timezone-with-data.js"></script>
		<script type="text/javascript" charset="utf-8" src="/plantao/resources/jquery/calendar/fullcalendar/v3.6.2/fullcalendar.min.js"></script>
		<script type="text/javascript" charset="utf-8" src="/plantao/resources/jquery/calendar/fullcalendar/v3.6.2/locale-all.js"></script>

		<ps:import-css uri="/resources/jquery/calendar/fullcalendar/v3.6.2/fullcalendar.css"/>
		<link media="print" rel="stylesheet" type="text/css" href="/plantao/resources/jquery/calendar/fullcalendar/v3.6.2/fullcalendar.print.css"/>
		<ps:import-css uri="/resources/css/skin/calendario.css"/>
		<ps:import-css uri="/resources/css/plantao.css"/>
		<ps:import-script relative="false" uri="/portalweb/resources/js/contextActions.js"/>
		<ps:import-script uri="/resources/js/agendamentoPlantao/agendamentoPlantao.js"/>

    </head>
 
    <body>
    	<ps:notification-fields/>
    	<input type="hidden" id="idLocal" value="${idLocal}"/>
    	<input type="hidden" id="nomeUnidade" value="${local.localAtendimento.apelido}"/>
    	<input type="hidden" id="modeloCriacaoPlantao" value="${local.modeloCriacaoPlantao}"/>
		<div class="page-right">
			<ps:header label="Planejamento de plantões" fontIcon="fa-calendar" backHistory="backHistory"/>
			<plantao:header-common-local local="${local}"/>
			<section>

				<div id="filtros-area" class="filtros-area hide">

					<div class="modal-area">
						<header class="modal template flip-header">
							<label>Filtro customizado</label>
						</header>
						<div class="modal-content filtro-content">
							<ps:form-container>
								<ps:form-column>
									<ps:form-field>
										<h2 class="modal"><i class="fa fa-user-md"></i>Profissional</h2>
										<select id="profissionais" placeholder="Pesquisar profissional" class="modal"></select>
									</ps:form-field>
									<ps:form-field>
										<h2 class="modal"><i class="fa fa-stethoscope"></i>Especialidade</h2>
										<select id="especialidade" placeholder="Selecionar especialidade" class="modal">
											<option></option>
											<c:forEach  var="especialidade" items="${especialidades}">
												<option ${especialidade.id == especialidadeBase.id ? 'selected' : ''} value="${especialidade.id}">${especialidade.nome}</option>
											</c:forEach>
										</select>
									</ps:form-field>
									<ps:form-field>
										<h2 class="label-obrigatorio modal"><i class="fa fa-pw-relational-graph"></i>Sub-especialidade</h2>
										<select id="subespecialidade" placeholder="Selecionar Sub-especialidades" class="modal"></select>
									</ps:form-field>
									<ps:form-field>
										<h2 class="modal"><i class="fa fa-user"></i>Criado por quem</h2>
										<select id="criadores" placeholder="Pesquisar usuário" class="modal"></select>
									</ps:form-field>
								</ps:form-column>
							</ps:form-container>
						</div>
						<footer class="modal">
							<button type="button" id="redefinir" class="action">Redefinir</button>
							<button type="button" id="pesquisar" class="default-action blue">Pesquisar</button>
						</footer>
					</div>
				</div>

				<div id="calendario"></div>
				<div id="plantao-area">
					<div id="plantao-opcoes"></div>
					<div id="plantao-calendario"></div>
				</div>
			</section>		
		</div>
		<ps:footer>
			<ps:footer-default-button label="Criar plantão" icon="add" id="criar"/>
			<ps:footer-actions-buttons-area>
				<li class="fixed">
					<button id="back" class="item-action-bt" type="button"><i class="fa fa-angle-left"></i></button>
				</li>
				<li class="fixed">
					<button id="prox" class="item-action-bt" type="button"><i class="fa fa-angle-right"></i></button>
				</li>
			</ps:footer-actions-buttons-area>
		</ps:footer>
    </body>
</html>
