SELECT ftc.CompanyCode,coalesce(NewsCount,0),coalesce(SpotPriceCount,0),coalesce(OpeningPriceCount,0),coalesce(AbsoluteChangeCount,0),coalesce(ClosingPriceCount,0),coalesce(percentageChangeCount,0),coalesce(newsAdjustment,0),coalesce(SpotPriceAdjustment,0),coalesce(OpeningPriceAdjustment,0),coalesce(AbsoluteChangeAdjustment,0),coalesce(ClosingPriceAdjustment,0),coalesce(percentageChangeAdjustment,0)
FROM FTSECompanies ftc
LEFT OUTER JOIN CompanyNewsCount cnc ON (cnc.CompanyCode = ftc.CompanyCode)
LEFT OUTER JOIN CompanySpotPriceCount csc ON (csc.CompanyCode = ftc.CompanyCode)
LEFT OUTER JOIN CompanyOpeningPriceCount coc ON (coc.CompanyCode = ftc.CompanyCode)
LEFT OUTER JOIN CompanyAbsoluteChangeCount cac ON (cac.CompanyCode = ftc.CompanyCode)
LEFT OUTER JOIN CompanyClosingPriceCount ccc ON (ccc.CompanyCode = ftc.CompanyCode)
LEFT OUTER JOIN CompanyPercentageChangeCount cpc ON (cpc.CompanyCode = ftc.CompanyCode)
;
