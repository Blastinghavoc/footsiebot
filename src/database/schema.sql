DROP TABLE FTSECompanies;
DROP TABLE FTSECompanySnapshots;
DROP TABLE FTSEGroupMappings;
DROP TABLE Queries;
DROP TABLE CompanyNewsCount;
DROP TABLE CompanySpotPriceCount;
DROP TABLE CompanyOpeningPriceCount;
DROP TABLE CompanyAbsoluteChangeCount;
DROP TABLE CompanyClosingPriceCount;
DROP TABLE PercentageChangeCount;
DROP TABLE AISettings;

CREATE TABLE FTSECompanies (
	CompanyCode varchar(10),
	CompanyName varchar(30),
	primary key(CompanyCode)
);

CREATE TABLE FTSECompanySnapshots (
	CompanyCode varchar(10),
	SpotPrice decimal(5,2),
	PercentageChange decimal(2,2),
	AbsoluteChange decimal(4,2),
	TimeOfData TimeStamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	primary key(CompanyCode, TimeOfData),
	foreign key (CompanyCode) references FTSECompanies(CompanyCode)
);

CREATE TABLE FTSEGroupMappings (
	GroupName varchar(40),
	CompanyCode varchar(10),
	primary key(GroupName, CompanyCode),
	foreign key (CompanyCode) references FTSECompanies(CompanyCode)
);

CREATE TABLE Queries (
	CompanyCode varchar(10),
	TimeOfQuery TimeStamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	Intent varchar(30),
	TimeSpecifier varchar(30),
	primary key(CompanyCode, TimeOfQuery),
	foreign key (CompanyCode) references FTSECompanies(CompanyCode)
);

CREATE TABLE CompanyNewsCount (
	CompanyCode varchar(10),
	NewsCount integer,
	newsAdjustment decimal(2,2),
	primary key(CompanyCode),
	foreign key (CompanyCode) references FTSECompanies(CompanyCode)
);

CREATE TABLE CompanySpotPriceCount (
	CompanyCode varchar(10),
	SpotPriceCount integer,
	SpotPriceAdjustment decimal(2,2),
	primary key(CompanyCode),
	foreign key (CompanyCode) references FTSECompanies(CompanyCode)
);

CREATE TABLE CompanyOpeningPriceCount (
	CompanyCode varchar(10),
	OpeningPriceCount integer,
	OpeningPriceAdjustment decimal(2,2),
	primary key(CompanyCode),
	foreign key (CompanyCode) references FTSECompanies(CompanyCode)
);

CREATE TABLE CompanyAbsoluteChangeCount (
	CompanyCode varchar(10),
	AbsoluteChangeCount integer,
	AbsoluteChangeAdjustment decimal(2,2),
	primary key(CompanyCode),
	foreign key (CompanyCode) references FTSECompanies(CompanyCode)
);

CREATE TABLE CompanyClosingPriceCount (
	CompanyCode varchar(10),
	ClosingPriceCount integer,
	ClosingPriceAdjustment decimal(2,2),
	primary key(CompanyCode),
	foreign key (CompanyCode) references FTSECompanies(CompanyCode)
);

CREATE TABLE CompanyPercentageChangeCount (
	CompanyCode varchar(10),
	PercentageChangeCount integer,
	PercentageChangeAdjustment decimal(2,2),
	primary key(CompanyCode),
	foreign key (CompanyCode) references FTSECompanies(CompanyCode)
);

CREATE TABLE AISettings (
	NewsTime integer,
	SpotPriceChange decimal(2,2),
	StartUpNum integer,
	primary key(NewsTime)
);
