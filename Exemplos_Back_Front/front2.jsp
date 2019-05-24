<%@page contentType="text/html; charset=ISO-8859-1" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="ps" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="plantao" tagdir="/WEB-INF/tags/plantao" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
 
<html>
 
    <head>
        <title>Prevent Senior :: Cadastro de tipo de plantao</title>
		<ps:css-default-import/><ps:css-skin-import/>
		<ps:jquery-default-import/>
		<ps:jquery-ui-import/>
		<ps:jquery-notification-import/>
		<ps:jquery-tags-selectize-import/>
		<script type="text/javascript" charset="utf-8" src="/portalweb/resources/jquery/moment.min.js"></script>
		<script type="text/javascript" charset="utf-8" src="/portalweb/resources/jquery/moment-timezone-with-data.js"></script>
		<script type="text/javascript" charset="utf-8" src="/portalweb/resources/js/contextActions.js"></script>
		<script type="text/javascript" charset="utf-8" src="<c:url value='/resources/js/cadastroProfissional/cadastrarProfissional.js?version=${versaoSistema.commit}'/>"></script>
		<link href='<c:url value='/resources/css/plantao.css'/>' rel='stylesheet' />
		<ps:masks/>
		<style type="text/css">
			.listaPrestador{
				display: none;
			}
		</style>
    </head>
 
    <body>
    	<form id="form" method="post">
	    	<ps:notification-fields/>
			<div class="page-right">
				<ps:header fontIcon="fa-user-md" label="Cadastro de profissionais" back="back"/>
				<input type="hidden" id="validarProfissional" value="${validarProfissional}">
				
				<input type="hidden" id="unidade" value="${local.id}" name="local.id">
				<input type="hidden" id="idLocal" value="${local.localAtendimento.id}" name="local.localAtendimento.id">
				<input type="hidden" id="idLocalPlantao" value="${local.id}" name="local.id">
				<input type="hidden" id="idProfissional" value="${profissional.id}"/>
				<section>
					<h1 class="normal-color">Informações do local</h1>
					<plantao:header-pagina/>
				</section>			
				<h1 class="normal-color">Informações do profissional</h1>
				<ps:form-fieldset extraClasses="light-color extreme-light-back">
					<ps:form-container>
						<ps:form-column>
							<ps:form-field>
								<label class="default">Profissional</label>
								<select name="profissional.id" id="profissional" class="obrigatorio">
									<c:if test="${profissional != null}">
										<option value="${profissional.id}" >${profissional.nome}</option>
									</c:if>
								</select>
							</ps:form-field>
						</ps:form-column>
						<ps:form-column>
							<ps:form-field>
								<label class="default">Empresa</label>
								<select name="prestador.id" id="empresa" class="obrigatorio">
									<c:if test="${listaPrestadores != null}">
										<c:forEach var="prest" items="${listaPrestadores}" >
										<option ${prestador.id == prest.id ? 'selected="selected"' : ''} value="${prest.id}" >${prest.apelido}</option>
										</c:forEach>
									</c:if>
								</select>
							</ps:form-field>
						</ps:form-column>
					</ps:form-container>
				</ps:form-fieldset>
				<h1 class="normal-color">Tipos de plantões</h1>
				<section>
					<table id="tabela" class="tabela-vigencia default-table">
						<thead>
							<tr>
								<th class="size-2">Status</th>
								<th>Plantão</th>
								<th class="area-vigencia center">Período de pagamento</th>
								<th class="size-3 center">Ações</th>
							</tr>
						</thead>
						
						<tbody>
							<c:set var="mostrarBotoesCadastro" value="${mostrarBotaoCadastro or permissaoHelper.validaPermissao('plantao_vigencia_profissional')}"/>
							<c:forEach var="tipo" items="${tiposPlantaoBD}" varStatus="i">
								<c:set var="position" value="0"/>
								<c:set var="pp" value="false"/>
								<c:forEach var="p" items="${permissoes}">
									<c:if test="${p.id.toUpperCase() == tipo.nome.toUpperCase()}">
										<c:set var="pp" value="true"/>
									</c:if>
								</c:forEach>
								<tr class="odd gradeX item-table-action hover-table">
									<td class="status-tipo-plantao center" title="${tipo.ativo ? 'Ativo' : 'Inativo'}"><i class="fa ${tipo.ativo ? 'fa-check-circle' : 'fa-ban'}"></i></td>
									<td>${tipo.nome}</td>
									<td data-tipo-plantao="${tipo.id}" class="dados-vigencia">
										<c:forEach var="vigencia" items="${vigencias}" varStatus="j">
											<c:if test="${vigencia.id.tipoPlantao.id == tipo.id}">
												<div class="linha-vigencia">
													<c:set var="position" value="${position+1}"/>
													<fmt:formatDate var="inicio" pattern="dd/MM/yyyy" value="${vigencia.id.dataInicio}" />
													<fmt:formatDate var="fim" pattern="dd/MM/yyyy" value="${vigencia.dataFim}" />
													<div class="vig-0">${position}</div>
													<div class="vig-1">de</div>
													<div class="vig-2"><input type="text" data-original="${inicio}" name="vigencias[${j.index}].id.dataInicio" class="inicio center input-table obrigatorio ${mostrarBotoesCadastro && pp ? 'data' : 'disabled'}" value="${inicio}" ${mostrarBotoesCadastro && pp ? '' : "readonly='readonly'"}></div>
													<div class="vig-3">até</div>
													<div class="vig-4"><input type="text" data-original="${fim}" name="vigencias[${j.index}].dataFim" class="fim center input-table obrigatorio ${mostrarBotoesCadastro && pp ? 'data' : 'disabled'}" value="${fim}" ${mostrarBotoesCadastro && pp ? '' : "readonly='readonly'"}></div>
													<div class="vig-7">Valor</div>
													<div class="vig-8"><input type="text" name="vigencias[${j.index}].valorReais" class="right input-table obrigatorio monetario ${mostrarBotoesCadastro && pp ? '' : 'disabled'}" value="<fmt:formatNumber value="${vigencia.valor}" type="currency" currencySymbol="R$" />" ${mostrarBotoesCadastro && pp ? '' : "readonly='readonly'"}></div>
													<div class="vig-9">Pagar por</div>
													<div class="vig-10">
														<input type="hidden" name="vigencias[${j.index}].id.tipoPlantao.id" class="tipoPlantao" value="${tipo.id}">
														<select name="vigencias[${j.index}].calculoPorHoras" class="input-table calculo obrigatorio tipoPagamento ${mostrarBotoesCadastro && pp ? '' : 'disabled'}" ${mostrarBotoesCadastro && pp ? '' : "disabled='disabled'"}>
															<option value="">Selecione...</option>
															<option ${vigencia.calculoPorHoras ? 'selected="selected"' : ''} value="true">Horas</option>
															<option ${vigencia.calculoPorHoras ? '' : 'selected="selected"'} value="false">Valor fechado</option>
														</select>
														<c:if test="${mostrarBotoesCadastro == false || pp == false}">
															<input type="hidden" name="vigencias[${j.index}].calculoPorHoras" value="${vigencia.calculoPorHoras}">
														</c:if>
													</div>
													<div class="vig-11 "><button title="Remover período" class="remove-vigencia red tabela-button ${mostrarBotoesCadastro && pp ? '' : 'hide'}" type="button"><i class="fa fa-trash"></i></button></div>
													<div class="aviso-erro"></div>
												</div>
											</c:if>										
										</c:forEach>
									</td>
									<td class="center">
										<button title="Adicionar período de pagamento" class="add-vigencia blue tabela-button ${mostrarBotoesCadastro && pp ? '' : 'hide'}" type="button"><i class="fa fa-plus"></i></button>
									</td>
								</tr>
							</c:forEach>						
						</tbody>
						<tfoot>
						</tfoot>
					</table>
				</section>
			</div>
		</form>
		<div class="hide">
			<div id="template-vigencia" class="linha-vigencia">
				<div class="vig-0"></div>
				<div class="vig-1">de</div>
				<div class="vig-2"><input type="text" data-original="" name="vigencias[9999999999].id.dataInicio" class="inicio center input-table obrigatorio calendario" value=""></div>
				<div class="vig-3">até</div>
				<div class="vig-4"><input type="text" data-original="" name="vigencias[999999999].dataFim" class="fim center input-table obrigatorio calendario" value=""></div>
				<div class="vig-7">Valor</div>
				<div class="vig-8"><input type="text" name="vigencias[999999999].valorReais" class="right input-table obrigatorio monetario" value=""></div>
				<div class="vig-9">Pagar por</div>
				<div class="vig-10">
					<input type="hidden" name="vigencias[99999999].id.tipoPlantao.id" class="tipoPlantao" value="">
					<select name="vigencias[999999999].calculoPorHoras" class="input-table calculo obrigatorio">
						<option selected="selected" value="">Selecione...</option>
						<option value="true">Horas</option>
						<option value="false">Valor fechado</option>
					</select>
				</div>
				<div class="vig-11 ${mostrarBotoesCadastro ? '' : 'hide'}"><button title="Remover período" class="remove-vigencia red tabela-button" type="button"><i class="fa fa-trash"></i></button></div>
				<div class="aviso-erro"></div>
			</div>
		</div>
		<c:if test="${mostrarBotoesCadastro}">
			<ps:footer>
					<ps:footer-default-button label="salvar" icon="save" id="salvar"/>
				<ps:footer-actions-buttons-area>
					<ps:footer-actions-button label="Visualizar detalhes" icon="eye" id="ver"/>
				</ps:footer-actions-buttons-area>
			</ps:footer>
		</c:if>
    </body>
</html>
