<!DOCTYPE html>
<html xmlns="http://www.w3.org/TR/REC-html40" 
	xmlns:v="urn:schemas-microsoft-com:vml" 
	xmlns:o="urn:schemas-microsoft-com:office:office" 
	xmlns:w="urn:schemas-microsoft-com:office:word" 
	xmlns:m="http://schemas.microsoft.com/office/2004/12/omml">
<head>
  <meta name="ProgId" content="Word.Document" />
  <meta name="Generator" content="Microsoft Word 12" />
  <meta name="Originator" content="Microsoft Word 12" /> 
<!--[if gte mso 9]><xml><w:WordDocument><w:View>Print</w:View></w:WordDocument></xml><[endif]-->

  <meta charset="UTF-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>APIDEMO</title>
  <style type="text/css">
    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }

    .detail-container {
      background-color: #eaeff4;
      padding: 16px;
    }

    .detail-container__item {
      background: #fff;
      border-radius: 3px;
      line-height: normal;
      padding: 16px;
    }

    .detail-container__item-title {
      color: #494e51;
      font-size: 20px;
      font-weight: 700;
      padding-bottom: 12px;
    }

    .mb-2 {
      margin-bottom: 16px !important;
    }

    .gs-row {
      position: absolute;
	  width: 100%
    }

    .gs-row::after {
      display: table;
      content: "";
      clear: both;
    }

    .gs-col {
      float: left;
    }

    .gs-col-6 {
      width: 50%;
	  display：block;
    }

    .gs-col-12 {
      width: 50%;
    }

    .base-info {
      margin-bottom: 16px;
    }

    .detail-container__item-base {
      color: #626669;
      font-size: 12px;
    }

    .api-data-info .detail-container__item-container {
      display: flex;
      color: #626669;
      font-size: 12px;
      margin-left: -23px;
      padding-bottom: 8px;
      flex-wrap: wrap;
    }

    .strategy-wrapper {
      display: flex;
    }

    .detail-container__item-label {
      margin-bottom: 16px;
      padding: 0 23px;
      position: relative;
    }

    .detail-container__item-label::before {
      background-color: #e2e2e2;
      content: '';
      display: block;
      height: 13px;
      left: 0;
      position: absolute;
      top: 50%;
      -webkit-transform: translate(0, -50%);
      transform: translate(0, -50%);
      width: 2px;
    }

    .gs-tables-title {
      position: relative;
      background: #fafafa;
      padding: 0 16px;
      font-size: 12px;
      text-align: left;
      font-weight: 700;
      color: #000;
      height: 40px;
    }

    .gs-tables-data {
      background: #fff;
      height: 40px;
      padding: 8px 16px;
      border-bottom: 1px solid #e6e6e6;
      text-align: left;
      font-size: 0;
      position: relative;
    }

    .gs-tables-title-content {
      display: inline-block;
      vertical-align: top;
      word-break: break-all;
    }

    .table-main {
      border: 1px solid #e6e6e6;
      border-bottom: none;
      border-spacing: 0;
      border-collapse: separate;
      color: #666666;
    }

    .gs-tables-data:not(:last-of-type),
    .gs-tables-title:not(:last-of-type) {
      border-right: 1px solid #e6e6e6;
    }

    .gs-tables-title {
      border-bottom: 1px solid #e6e6e6;
    }

    .gs-tables-data-content {
      position: relative;
      z-index: 0;
      width: 100%;
      font-size: 12px;
      display: inline-block;
      max-width: 100%;
      vertical-align: text-bottom;
      overflow: hidden;
      word-break: break-all;
    }
	.turn{
	  width: 10px
      word-break: break-all;
    }
	.t{
		table-layout:fixed;
		word-break:break-all;
		word-wrap: break-word;
	}
  </style>
</head>

<body>
<#list list as item>
  <div class="detail-container">
    <div class="detail-container__item api-data-info">
      <div class="detail-container__item-title">
        基本信息
      </div>
	  <div class="detail-container__item-container">
			<table style="border: none; width: 100%;" class="t">
				<tr>
					<td class="turn" style='border:none;'>API名称：${item.name}</td>
					<td class="turn" style='border:none;'>描述：${item.description}</td>
				</tr>
				<tr>
					<td class="turn" style='border:none;'>发布状态：${item.status}</td>
					<td class="turn" style='border:none;'>所属目录：${item.categoryName}</td>
				</tr>
				<tr>
					<td class="turn" style='border:none;'>创建人：${item.creator}</td>
					<td class="turn" style='border:none;'>创建时间：${item.createTime}</td>
				</tr>
				<tr>
					<td class="turn" style='border:none;'>更新人：${item.updater}</td>
					<td class="turn" style='border:none;'>更新时间：${item.updateTime}</td>
				</tr>
				<tr>
					<td class="turn" style='border:none;'>版本：${item.version}</td>
					<td class="turn" style='border:none;'>策略控制：${item.apiIpRestriction.type}</td>
				</tr>
				<tr>
					<td class="turn" style='border:none;' colspan="2">
						配置策略：
						<#list item.apiIpRestriction.ipRestrictionNames as ipRestrictionName>
							${ipRestrictionName}
							<#if ipRestrictionName_has_next>、</#if>
						</#list>
					</td>
				</tr>
			</table>
	  </div>
    </div>
    <div class="detail-container__item api-data-info">
      <div class="detail-container__item-title">
        API数据信息
      </div>
      <div class="detail-container__item-container">
			<table style="border: none; width: 100%;" class="t">
				<tr>
					<td class="turn" style='border:none;'>数据源类型: ${item.sourceType}</td>
					<td class="turn" style='border:none;'>参数协议: ${item.protocol}</td>
				</tr>
				<tr>
					<td class="turn" style='border:none;'>请求方式: ${item.requestMode}</td>
					<td class="turn" style='border:none;'>API路径: ${item.path}</td>
				</tr>
				<tr>
					<td class="turn" style='border:none;'>数据表: ${item.tableName}</td>
					<td class="turn" style='border:none;'>数据库: ${item.dbName}</td>
				</tr>
				<tr>
					<td class="turn" style='border:none;' colspan="2">${item.sourceName}</td>
				</tr>
			</table>
      </div>
      <div class="gs-tables">
        <table class="table-main" style="width: 100%;">
          <thead class="gs-tables-head">
            <tr class="gs-tables-row">
				<th rowspan="1" class="gs-tables-title " style="text-align: left;">
				<span class="gs-tables-title-content" style="text-align: left;">字段名称</span></th>
				<th rowspan="1" class="gs-tables-title " style="text-align: left;"><span class="gs-tables-title-content"
				  style="text-align: left;">筛选字段</span></th>
				<th rowspan="1" class="gs-tables-title " style="text-align: left;"><span class="gs-tables-title-content"
				  style="text-align: left;">必传</span></th>
				<th rowspan="1" class="gs-tables-title " style="text-align: left;"><span class="gs-tables-title-content"
				  style="text-align: left;">缺省值</span></th>
				<th rowspan="1" class="gs-tables-title " style="text-align: left;"><span class="gs-tables-title-content"
				  style="text-align: left;">脱敏规则</span></th>
            </tr>
          </thead>
          <tbody>
			<#list item.columns as column>
				<tr draggable="false" class="gs-tables-row">
					<td class="turn" class="gs-tables-data">
					<span style="font-size: 0px; vertical-align: top;"><span class="gs-tables-data-content">
						${column.name}
					  </span></span></td>
					<td class="turn" class="gs-tables-data"><span style="font-size: 0px; vertical-align: top;"><span
						class="gs-tables-data-content"><span>
						  ${column.filter}
						</span></span></span></td>
					<td class="turn" class="gs-tables-data"><span style="font-size: 0px; vertical-align: top;"><span
						class="gs-tables-data-content"><span>
						  ${column.need}
						</span></span></span></td>
					<td class="turn" class="gs-tables-data"><span style="font-size: 0px; vertical-align: top;"><span
						class="gs-tables-data-content"><span>
						  ${column.defaultValue}
						</span></span></span></td>
					<td class="turn" class="gs-tables-data"><span style="font-size: 0px; vertical-align: top;"><span
						class="gs-tables-data-content"><span>
						  ${column.rule}
						</span></span></span></td>
				</tr>
			</#list> 
          </tbody>
        </table>
      </div>
    </div>
  </div>
  <br><br>
</#list>
</body>

</html>