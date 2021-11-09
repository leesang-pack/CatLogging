TRUNCATE TABLE LANGUAGES;

INSERT INTO LANGUAGES (LOCALE, MESSAGEKEY,MESSAGECONTENT) VALUES
-- #Form
('en', 'catlogging.common.form.main','Main'),
('ko', 'catlogging.common.form.main','처음'),
('zh', 'catlogging.common.form.main','主要的'),

('en', 'catlogging.common.form.reader','Reader'),
('ko', 'catlogging.common.form.reader','읽는 방법'),
('zh', 'catlogging.common.form.reader','如何阅读'),

('en', 'catlogging.common.form.filters','Filters'),
('ko', 'catlogging.common.form.filters','필터'),
('zh', 'catlogging.common.form.filters','筛选'),

('en', 'catlogging.common.form.filter','Filter'),
('ko', 'catlogging.common.form.filter','필터'),
('zh', 'catlogging.common.form.filter','筛选'),

('en', 'catlogging.common.form.testScan','Test scanning'),
('ko', 'catlogging.common.form.testScan','테스트 스캔'),
('zh', 'catlogging.common.form.testScan','测试扫描'),

('en', 'catlogging.common.form.eventScanner','Event scanner configuration'),
('ko', 'catlogging.common.form.eventScanner','이벤트 스캐너 구성'),
('zh', 'catlogging.common.form.eventScanner','事件扫描器配置'),

('en', 'catlogging.common.form.name','Name'),
('ko', 'catlogging.common.form.name','이름'),
('zh', 'catlogging.common.form.name','姓名'),

('en', 'catlogging.common.form.create','Create'),
('ko', 'catlogging.common.form.create','생성'),
('zh', 'catlogging.common.form.create','生产'),

('en', 'catlogging.common.form.start','Start'),
('ko', 'catlogging.common.form.start','시작'),
('zh', 'catlogging.common.form.start','开始'),

('en', 'catlogging.common.form.pause','Pause'),
('ko', 'catlogging.common.form.pause','멈춤'),
('zh', 'catlogging.common.form.pause','开始'),

('en', 'catlogging.common.form.save','Save'),
('ko', 'catlogging.common.form.save','저장'),
('zh', 'catlogging.common.form.save','节省'),

('en', 'catlogging.common.form.edit','Edit'),
('ko', 'catlogging.common.form.edit','수정'),
('zh', 'catlogging.common.form.edit','更正'),

('en', 'catlogging.common.form.control','Control'),
('ko', 'catlogging.common.form.control','컨트롤'),
('zh', 'catlogging.common.form.control','控制'),

('en', 'catlogging.common.form.publishers','Publishers'),
('ko', 'catlogging.common.form.publishers','퍼블리셔'),
('zh', 'catlogging.common.form.publishers','发布'),

('en', 'catlogging.common.form.scanner','Scanner'),
('ko', 'catlogging.common.form.scanner','스캐너'),
('zh', 'catlogging.common.form.scanner','扫描器'),

('en', 'catlogging.common.form.viewer','Viewer'),
('ko', 'catlogging.common.form.viewer','보기'),
('zh', 'catlogging.common.form.viewer','看'),

('en', 'catlogging.common.form.info','Info'),
('ko', 'catlogging.common.form.info','정보'),
('zh', 'catlogging.common.form.info','信息'),

('en', 'catlogging.common.form.pattern','Pattern'),
('ko', 'catlogging.common.form.pattern','패턴'),
('zh', 'catlogging.common.form.pattern','图案'),

('en', 'catlogging.common.form.patternFlags','Pattern flags'),
('ko', 'catlogging.common.form.patternFlags','패턴 옵션'),
('zh', 'catlogging.common.form.patternFlags','模式选项'),

('en', 'catlogging.common.form.patternCaseMatch','Case-insensitive matching'),
('ko', 'catlogging.common.form.patternCaseMatch','대소문자를 구분하지 않음'),
('zh', 'catlogging.common.form.patternCaseMatch','不区分大小写'),

('en', 'catlogging.common.form.patternSubString','Sub string search'),
('ko', 'catlogging.common.form.patternSubString','하위 문자열 검색'),
('zh', 'catlogging.common.form.patternSubString','子串搜索'),

('en', 'catlogging.common.form.patternMultiline','Multiline mode'),
('ko', 'catlogging.common.form.patternMultiline','멀티라인지원 모드'),
('zh', 'catlogging.common.form.patternMultiline','多线支持模式'),

('en', 'catlogging.common.form.patternDotAil','Dot All mode'),
('ko', 'catlogging.common.form.patternDotAil','Dot 모드'),
('zh', 'catlogging.common.form.patternDotAil','Dot 模式'),

('en', 'catlogging.common.form.characterSet','Character set'),
('ko', 'catlogging.common.form.characterSet','문자열 셋'),
('zh', 'catlogging.common.form.characterSet','字符串集'),

('en', 'catlogging.common.form.logEntryReader','Log entry reader'),
('ko', 'catlogging.common.form.logEntryReader','로그 엔트리 읽기'),
('zh', 'catlogging.common.form.logEntryReader','读取日志条目'),

('en', 'catlogging.common.form.overflowAttr','Overflow attribute'),
('ko', 'catlogging.common.form.overflowAttr','오버플로 속성'),
('zh', 'catlogging.common.form.overflowAttr','溢出属性'),

('en', 'catlogging.common.form.overflowAttr.text','In case of lines not matching the pattern these can be attached to a field of last well parsed log line.
				 The overflow field can reference an existing or a new field. If not set, the not matching lines will be attached (as default)
				 to the <code>lf_raw</code> field of the last well parsed log entry.'),
('ko', 'catlogging.common.form.overflowAttr.text','패턴과 일치하지 않는 행의 경우 마지막으로 잘 구문 분석된 로그 행의 필드에 첨부할 수 있습니다.
오버플로 필드는 기존 필드 또는 새 필드를 참조할 수 있으며 설정하지 않으면 일치하지 않는 행이 첨부됩니다(기본값).
마지막으로 잘 구문 분석된 로그 항목의 <code>lf_raw</code> 필드'),
('zh', 'catlogging.common.form.overflowAttr.test','如果行与模式不匹配，这些可以附加到最后一个解析良好的日志行的字段.
溢出字段可以引用现有字段或新字段. 如果未设置，将附加不匹配的行（默认）
到最后一个解析良好的日志条目的 <code>lf_raw</code> 字段.'),

('en', 'catlogging.common.form.test.resolve','Test resolving logs'),
('ko', 'catlogging.common.form.test.resolve','테스트 로그파일'),
('zh', 'catlogging.common.form.test.resolve','测试日志文件'),

('en', 'catlogging.common.form.test.view','Test log viewing'),
('ko', 'catlogging.common.form.test.view','테스트 로그파일 뷰'),
('zh', 'catlogging.common.form.test.view','测试日志文件查看'),

-- #Wizards
('en', 'catlogging.wizard.scanner.level','Severity level scanner'),
('ko', 'catlogging.wizard.scanner.level','로그 레벨 단위 스캐너'),
('zh', 'catlogging.wizard.scanner.level','严重级别扫描器'),

('en', 'catlogging.wizard.scanner.regexPattern','Regular expression scanner'),
('ko', 'catlogging.wizard.scanner.regexPattern','정규식 스캐너'),
('zh', 'catlogging.wizard.scanner.regexPattern','正则表达式扫描器'),

('en', 'catlogging.wizard.scanner.filter.entriesFieldsDumper','Entry fields copying'),
('ko', 'catlogging.wizard.scanner.filter.entriesFieldsDumper','항목 필트 복사'),
('zh', 'catlogging.wizard.scanner.filter.entriesFieldsDumper','目字段复制'),

('en', 'catlogging.wizard.publisher.mail','Mail notification'),
('ko', 'catlogging.wizard.publisher.mail','메일 알람'),
('zh', 'catlogging.wizard.publisher.mail','邮件通知'),

('en', 'catlogging.wizard.publisher.http','HTTP publishing'),
('ko', 'catlogging.wizard.publisher.http','HTTP 퍼블리셔'),
('zh', 'catlogging.wizard.publisher.http','HTTP 发布'),

('en', 'catlogging.wizard.publisher.shellCommand','Shell command'),
('ko', 'catlogging.wizard.publisher.shellCommand','쉘 명령'),
('zh', 'catlogging.wizard.publisher.shellCommand','Shell 命令'),

('en', 'catlogging.wizard.readerStrategy.minByteAmount','Minimum size per iteration'),
('ko', 'catlogging.wizard.readerStrategy.minByteAmount','반복 최소 단위(byte)'),
('zh', 'catlogging.wizard.readerStrategy.minByteAmount','每次迭代的最小大小'),

('en', 'catlogging.wizard.source.file.wildcard','Simple log files'),
('ko', 'catlogging.wizard.source.file.wildcard','간단한 방식 로그검출'),
('zh', 'catlogging.wizard.source.file.wildcard','简单日志文件'),

('en', 'catlogging.wizard.source.file.timestampRollingStaticLiveName','Rolling log files with static live file name'),
('ko', 'catlogging.wizard.source.file.timestampRollingStaticLiveName','타임스탬프 기반 정적 로그 파일 검출'),
('zh', 'catlogging.wizard.source.file.timestampRollingStaticLiveName','带有静态实时文件名的滚动日志文件'),

('en', 'catlogging.wizard.source.file.timestampRollingDynamicLiveName','Rolling log file with dynamic live file name'),
('ko', 'catlogging.wizard.source.file.timestampRollingDynamicLiveName','타임스탬프 기반 동적 로그 파일 검출'),
('zh', 'catlogging.wizard.source.file.timestampRollingDynamicLiveName','带有动态实时文件名的滚动日志文件'),

('en', 'catlogging.wizard.source.compoundLogSource','Compound log'),
('ko', 'catlogging.wizard.source.compoundLogSource','로그 검출 규칙 묶어서 관리'),
('zh', 'catlogging.wizard.source.compoundLogSource','复合日志'),

('en', 'catlogging.wizard.reader.log4j','Log4j conversion pattern reader'),
('ko', 'catlogging.wizard.reader.log4j','Log4j 방식으로 로그 읽음'),
('zh', 'catlogging.wizard.reader.log4j','Log4j 转换模式阅读器'),

('en', 'catlogging.wizard.reader.grok','Regex / Grok matching reader'),
('ko', 'catlogging.wizard.reader.grok','Regex / Grok 매칭 방식으로 로그 읽음'),
('zh', 'catlogging.wizard.reader.grok','Regex / Grok 匹配阅读器'),

('en', 'catlogging.wizard.reader.grok.text.1','This reader reads the content of a log and parses each line using the specified regular or grok expression.
		The below pattern is evaluated against each log line. <span ng-include="contextPath + ''/ng/help/regexGrokPattern.html?v=''+version"></span>'),
('ko', 'catlogging.wizard.reader.grok.text.1','이 판독기는 로그의 내용을 읽고 지정된 일반 또는 grok 표현식을 사용하여 각 행을 구문 분석합니다.
아래 패턴은 각 로그 라인에 대해 평가됩니다. <span ng-include="contextPath + ''/ng/help/regexGrokPattern.html?v=''+버전"></span>'),
('zh', 'catlogging.wizard.reader.grok.text.1','此读取器读取日志的内容并使用指定的正则或 grok 表达式解析每一行.
下面的模式是针对每个日志行进行评估的. <span ng-include="contextPath + ''/ng/help/regexGrokPattern.html?v=''+version"></span>'),

('en', 'catlogging.wizard.filter.severityMappingFilter','Severity mapping'),
('ko', 'catlogging.wizard.filter.severityMappingFilter','심각도 맵핑 필터'),
('zh', 'catlogging.wizard.filter.severityMappingFilter','严重性映射'),

('en', 'catlogging.wizard.filter.jsonParseFilter','JSON parsing'),
('ko', 'catlogging.wizard.filter.jsonParseFilter','JSON 맵핑 필터'),
('zh', 'catlogging.wizard.filter.jsonParseFilter','JSON 解析'),

('en', 'catlogging.wizard.filter.timestampConvert','Timestamp parsing'),
('ko', 'catlogging.wizard.filter.timestampConvert','타임스탬프 맵핑 필터'),
('zh', 'catlogging.wizard.filter.timestampConvert','时间戳解析'),

('en', 'catlogging.wizard.filter.regexFilter','Regular expression extraction'),
('ko', 'catlogging.wizard.filter.regexFilter','정규식 맵핑 필터'),
('zh', 'catlogging.wizard.filter.regexFilter','正则表达式提取'),

('en', 'catlogging.wizard.reader.conversion','Conversion pattern'),
('ko', 'catlogging.wizard.reader.conversion','패턴 전환'),
('zh', 'catlogging.wizard.reader.conversion','模式转换'),

('en','catlogging.wizard.reader.conversion.text.1','In your log4j configuration the &lt;i&gt;ConversionPattern&lt;/i&gt; parameter of the desired appender controls the contents of each line of output inside the log file.
				 In order to parse the content attributes correctly please insert the used &lt;i&gt;ConversionPattern&lt;/i&gt; into this field. For example:
				 &lt;code&gt;%d %-5p [%c] (%t) %m%n&lt;/code&gt;.
				 For more detail about the syntax, see the &lt;a href=''http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/PatternLayout.html'' target=''_blank''&gt;ConversionPattern&lt;/a&gt;
				 API documentation.'),
('ko','catlogging.wizard.reader.conversion.text.1','log4j 구성에서 &lt;i&gt;ConversionPattern&lt;/i&gt; 원하는 추가 매개변수는 로그 파일 내의 각 출력 라인의 내용을 제어합니다.
콘텐츠 속성을 올바르게 구문 분석하려면 사용된 &lt;i&gt;ConversionPattern&lt;/i&gt; 이 부분에. 예를 들어:
&lt;code&gt;%d %-5p [%c] (%t) %m%n&lt;/code&gt;.
구문에 대한 자세한 내용은 &lt;a href=''http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/PatternLayout.html'' target=''_blank''&gt;ConversionPattern&lt;/a&gt;
API 문서 참고.'),
('zh','catlogging.wizard.reader.conversion.text.1','在 log4j 配置中,&lt;i&gt;ConversionPattern&lt;/i&gt; 您想要控制日志文件中每个输出行的内容的附加参数.
要正确解析内容属性,请使用 &lt;i&gt;ConversionPattern&lt;/i&gt; 在这部分. 例如：
&lt;code&gt;%d %-5p [%c] (%t) %m%n&lt;/code&gt;.
语法细节参见 &lt;a href=''http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/PatternLayout.html'' target=''_blank''&gt;ConversionPattern&lt;/a&gt
请参阅 API 文档.'),

('en','catlogging.wizard.reader.conversion.label.1','Log4j Conversion Pattern'),
('ko','catlogging.wizard.reader.conversion.label.1','log4j 패턴 전환'),
('zh','catlogging.wizard.reader.conversion.label.1','log4j 模式转换'),

('en', 'catlogging.wizard.reader.conversion.mapping','Conversion specifier field mapping'),
('ko', 'catlogging.wizard.reader.conversion.mapping','필드 지정자 매핑'),
('zh', 'catlogging.wizard.reader.conversion.mapping','字段说明符映射'),

('en','catlogging.wizard.reader.conversion.mapping.text.1','To assign the extracted log parts to fields with legible names define for each used log4j conversion specifier
					a mapping to the desired field name. For example: &lt;br/&gt;&lt;code&gt;%c=category&lt;/code&gt;&lt;br/&gt;
					&lt;code&gt;%t=thread&lt;/code&gt;&lt;br/&gt;
					&lt;small&gt;Note: The specifier &lt;code&gt;%d&lt;/code&gt; and &lt;code&gt;%p&lt;/code&gt; are parsed and
					mapped additionally to the internal fields &lt;code&gt;lf_timestamp&lt;/code&gt; and &lt;code&gt;lf_severity&lt;/code&gt;
					by default.&lt;/small&gt;'),
('ko','catlogging.wizard.reader.conversion.mapping.text.1','추출된 로그 부분을 읽기 쉬운 이름을 가진 필드에 할당하려면 사용된 각 log4j 변환 지정자에 대해 정의하십시오.
원하는 필드 이름에 대한 매핑. 예: &lt;br/&gt;&lt;code&gt;%c=category&lt;/code&gt;&lt;br/&gt;
&lt;code&gt;%t=thread&lt;/code&gt;&lt;br/&gt;
&lt;small&gt;참고: 지정자 &lt;code&gt;%d&lt;/code&gt; 및 &lt;code&gt;%p&lt;/code&gt; 구문 분석되고
내부 필드 &lt;code&gt;lf_timestamp&lt;/code&gt;에 추가로 매핑됨 및 &lt;code&gt;lf_severity&lt;/code&gt;
기본적으로.&lt;/small&gt;'),
('zh','catlogging.wizard.reader.conversion.mapping.text.1','要将提取的日志部分分配给具有清晰名称的字段,请为每个使用的 log4j 转换说明符定义
到所需字段名称的映射. 例如:&lt;br/&gt;&lt;code&gt;%c=category&lt;/code&gt;&lt;br/&gt;
&lt;code&gt;%t=thread&lt;/code&gt;&lt;br/&gt;
&lt;small&gt;注意:说明符&lt;code&gt;%d&lt;/code&gt; 和&lt;code&gt;%p&lt;/code&gt; 被解析和
另外映射到内部字段<code>lf_timestamp</code>; 和&lt;code&gt;lf_severity&lt;/code&gt;
默认情况下.&lt;/small&gt;'),

('en','catlogging.wizard.reader.conversion.mapping.label.1','Conversion Specifier Mapping'),
('ko','catlogging.wizard.reader.conversion.mapping.label.1','변환할 지정자 맵'),
('zh','catlogging.wizard.reader.conversion.mapping.label.1','要转换的说明符映射'),

('en','catlogging.wizard.reader.conversion.mapping.text.2','Please configure first the proper conversion pattern.'),
('ko','catlogging.wizard.reader.conversion.mapping.text.2','먼저 변환 패턴을 구성이 필요합니다.'),
('zh','catlogging.wizard.reader.conversion.mapping.text.2','请先配置正确的转换模式.'),

('en','catlogging.wizard.filter.text.1','Used to filter events e.g. for field transformation, normalization etc.'),
('ko','catlogging.wizard.filter.text.1','이벤트를 필터링하는 데 사용됩니다. 필드 변환, 정규화 등.'),
('zh','catlogging.wizard.filter.text.1','用于过滤事件,例如 用于场变换、归一化等.'),

('en','catlogging.wizard.filter.text.2','Used to filter log entries e.g. for field transformation, normalization etc.'),
('ko','catlogging.wizard.filter.text.2','필드 변환, 정규화 등을 위해 로그 항목을 필터링하는 데 사용됩니다.'),
('zh','catlogging.wizard.filter.text.2','用于过滤日志条目,例如 用于场变换、归一化等.'),

('en','catlogging.wizard.eventScanner.text.1','Configures the scanner sniffing the log consecutively for new events'),
('ko','catlogging.wizard.eventScanner.text.1','새 이벤트에 대해 연속적으로 로그를 스니핑하는 스캐너를 구성합니다.'),
('zh','catlogging.wizard.eventScanner.text.1','配置扫描仪连续嗅探新事件的日志'),

('en','catlogging.wizard.eventScanner.add','Add new filter'),
('ko','catlogging.wizard.eventScanner.add','필터 추가'),
('zh','catlogging.wizard.eventScanner.add','添加过滤器'),

-- # Common

('en','catlogging.common.timestampLocale','Timestamp locale'),
('ko','catlogging.common.timestampLocale','지역의 타임스탬프'),
('zh','catlogging.common.timestampLocale','本地时间戳'),

('en','catlogging.common.timestampLocale.text.1','The locale whose date format symbols (e.g. month names) should be used during parsing the timestamp string.'),
('ko','catlogging.common.timestampLocale.text.1','타임스탬프 문자열을 구문 분석하는 동안 날짜 형식 기호(예: 월 이름)를 사용해야 하는 로케일입니다.'),
('zh','catlogging.common.timestampLocale.text.1','在解析时间戳字符串期间应使用其日期格式符号（例如月份名称）的语言环境.'),

('en','catlogging.common.timeZone','Time zone'),
('ko','catlogging.common.timeZone','시간 영역'),
('zh','catlogging.common.timeZone','时区'),

('en','catlogging.common.timeZone.text.1','When no time zone information is present in the input timestamp string,
you should explictly set the time zone it’s meant to refer to.'),
('ko','catlogging.common.timeZone.text.1','입력 타임스탬프 문자열에 표준 시간대 정보가 없으면 참조할 표준 시간대를 명시적으로 설정해야 합니다.'),
('zh','catlogging.common.timeZone.text.1','当输入时间戳字符串中不存在时区信息时,您应该明确设置它要引用的时区.'),

('en','catlogging.common.deriveInput','- Derived from input string -'),
('ko','catlogging.common.deriveInput','- 입력된 로케일 의해 파생됨 -'),
('zh','catlogging.common.deriveInput','- 由输入区域派生 -'),

('en','catlogging.common.defaultLocale','- System default locale -'),
('ko','catlogging.common.defaultLocale','- 시스템 기본 로케일 -'),
('zh','catlogging.common.defaultLocale','- 系统默认语言环境 -'),

('en','catlogging.common.pleaseSelect','- Please select -'),
('ko','catlogging.common.pleaseSelect','- 선택해 주세요 -'),
('zh','catlogging.common.pleaseSelect','- 请选择 -'),

('en','catlogging.common.all','All'),
('ko','catlogging.common.all','모두'),
('zh','catlogging.common.all','全部'),

('en','catlogging.common.instance','instance'),
('ko','catlogging.common.instance','인스턴스'),
('zh','catlogging.common.instance','实例'),

('en', 'catlogging.common.log', 'Log'),
('ko', 'catlogging.common.log', '로그'),
('zh', 'catlogging.common.log', '日志'),

('en', 'catlogging.common.type', 'Type'),
('ko', 'catlogging.common.type', '타입'),
('zh', 'catlogging.common.type', '类型'),

('en', 'catlogging.common.source', 'Source'),
('ko', 'catlogging.common.source', '소스'),
('zh', 'catlogging.common.source', '来源'),

('en','catlogging.common.new','New'),
('ko','catlogging.common.new','신규'),
('zh','catlogging.common.new','新的'),

('en','catlogging.common.sniffer','observer'),
('ko','catlogging.common.sniffer','감시'),
('zh','catlogging.common.sniffer','观察者'),

('en','catlogging.common.list.text.1','You have not created any observer'),
('ko','catlogging.common.list.text.1','이벤트 감시자를 만들지 않았습니다.'),
('zh','catlogging.common.list.text.1','我没有创建事件观察者.'),

('en','catlogging.common.list.text.2','You have not created log source'),
('ko','catlogging.common.list.text.2','로그 소스를 만들지 않았습니다.'),
('zh','catlogging.common.list.text.2','我没有创建日志源.'),

('en','catlogging.common.add','Add'),
('ko','catlogging.common.add','추가'),
('zh','catlogging.common.add','添加'),

('en','catlogging.common.delete','Delete'),
('ko','catlogging.common.delete','삭제'),
('zh','catlogging.common.delete','删除'),

('en','catlogging.common.lastofmodify','Time of last modification'),
('ko','catlogging.common.lastofmodify','마지막 수정 시간'),
('zh','catlogging.common.lastofmodify','最后修改时间'),

('en','catlogging.common.filename','File name'),
('ko','catlogging.common.filename','파일 이름'),
('zh','catlogging.common.filename','文档名称'),

('en','catlogging.common.fileWildcard.text.1','Source for simple log files matching a file name pattern'),
('ko','catlogging.common.fileWildcard.text.1','파일들의 이름 패턴으로 매칭하는 단순한 파일 로그 방식'),
('zh','catlogging.common.fileWildcard.text.1','按名称模式匹配文件的简单文件日志方法'),

('en', 'catlogging.common.fileWildcard.text.2','File path pattern'),
('ko', 'catlogging.common.fileWildcard.text.2','파일 경로 패턴'),
('zh', 'catlogging.common.fileWildcard.text.2','文件路径模式'),

('en', 'catlogging.common.fileWildcard.text.3','The value may be a simple path which
targets a single file or alternatively may contain an <a href="http://ant.apache.org/manual/dirtasks.html#patterns" target="_blank">Ant-style pattern</a>
expression to expose each matching file as a stand-alone log.'),
('ko', 'catlogging.common.fileWildcard.text.3','값은 다음과 같은 간단한 경로일 수 있습니다.
단일 파일을 대상으로 하거나 <a href="http://ant.apache.org/manual/dirtasks.html#patterns" target="_blank">Ant 스타일 패턴</a>을 포함할 수 있습니다.
일치하는 각 파일을 독립 실행형 로그로 노출하는 표현식입니다.'),
('zh', 'catlogging.common.fileWildcard.text.3','该值可能是一个简单的路径
以单个文件为目标,或者可能包含一个 <a href="http://ant.apache.org/manual/dirtasks.html#patterns" target="_blank">Ant 样式模式</a>
表达式将每个匹配的文件公开为一个独立的日志.'),

('en','catlogging.common.timestampStatic.label.1','Source for rolled log files with a static named live log file'),
('ko','catlogging.common.timestampStatic.label.1','정적 이름의 라이브 로그 파일이 있는 소스'),
('zh','catlogging.common.timestampStatic.label.1','带有静态名称的实时日志文件的源'),

('en','catlogging.common.timestampStatic.label.2','Suffix for rolled over files'),
('ko','catlogging.common.timestampStatic.label.2','롤오버된 파일의 접미사'),
('zh','catlogging.common.timestampStatic.label.2','翻转文件的后缀'),

('en','catlogging.common.orderRollFile','Order criteria for rolled files'),
('ko','catlogging.common.orderRollFile','롤링된 파일의 정렬 기준'),
('zh','catlogging.common.orderRollFile','按滚动文件排序'),

('en','catlogging.common.timestampStatic.label.4','Path pattern for the live log'),
('ko','catlogging.common.timestampStatic.label.4','라이브 로그의 경로 패턴'),
('zh','catlogging.common.timestampStatic.label.4','实时日志的路径模式'),

('en','catlogging.common.timestampStatic.text.1','This source concats a live actively written log file with older rolled over files into a single continuous log.
		The live log has a static name and it''s the single file which can change size. After it''s rolled, the file got renamed to
		a fix timestamp based name and no longer changed to enable exact navigation in the connected logs.
		<strong>Important:</strong> This source supports rolling only based on a timestamp name pattern, because this guarantees
		static names of rolled over files, which is the key for navigating in the connected files.'),
('ko','catlogging.common.timestampStatic.text.1','이 소스는 이전에 롤오버된 파일과 함께 활성 기록된 라이브 로그 파일을 단일 연속 로그로 연결합니다.
라이브 로그는 정적 이름을 가지며 크기를 변경할 수 있는 단일 파일입니다. 롤링 된 후 파일 이름이 다음으로 변경되었습니다.
타임스탬프 기반 이름을 수정하고 연결된 로그에서 정확한 탐색을 활성화하기 위해 더 이상 변경되지 않습니다.
<strong>중요:</strong> 이 소스는 타임스탬프 이름 패턴을 기반으로 한 롤링만 지원합니다.
연결된 파일을 탐색하기 위한 키인 롤오버된 파일의 정적 이름입니다.'),
('zh','catlogging.common.timestampStatic.text.1','此源将实时主动写入的日志文件与较旧的滚动文件合并为单个连续日志.
实时日志有一个静态名称,它是可以更改大小的单个文件. 滚动后,文件被重命名为
基于时间戳的修复名称,不再更改以在连接的日志中启用精确导航.
<strong>重要提示</strong>：此来源仅支持基于时间戳名称模式的滚动,因为这可以保证
滚动文件的静态名称,这是在连接文件中导航的关键.'),

('en','catlogging.common.timestampStatic.text.2','Reference here the path pattern for the live log file being actively written and rolled over once a certain condition is met. This source supports exposing of multiple dedicated rolling logs in the case of matching several live files.'),
('ko','catlogging.common.timestampStatic.text.2','특정 조건이 충족되면 활발하게 기록되고 롤오버되는 라이브 로그 파일의 경로 패턴을 여기에서 참조하십시오. 이 소스는 여러 라이브 파일이 일치하는 경우 여러 전용 롤링 로그 노출을 지원합니다.'),
('zh','catlogging.common.timestampStatic.text.2','在此处参考活动日志文件的路径模式,一旦满足特定条件，就会主动写入和滚动. 该源支持在匹配多个实时文件的情况下公开多个专用滚动日志.'),

('en','catlogging.common.timestampStatic.text.3','This suffix pattern is used to detect older rolled over files in dependency to the name and location of the live log file.
					Example: In case of a daily rolling file with the live log <code>server.log</code> and rolled over files like
					<code>server.log.2015-07-02</code>, <code>server.log.2015-07-01</code> etc. the pattern <code>.*</code>
					will match the old rolled over files and combine all together into a continuous log file.'),
('ko','catlogging.common.timestampStatic.text.3','그의 접미사 패턴은 라이브 로그 파일의 이름과 위치에 따라 이전 롤오버 파일을 감지하는 데 사용됩니다.
예: 라이브 로그 <code>server.log</code>가 있는 일일 롤링 파일과 다음과 같은 롤오버 파일의 경우
<code>server.log.2015-07-02</code>, <code>server.log.2015-07-01</code> 등 <code>.*</code> 패턴
이전 롤오버 파일과 일치하고 모두 함께 연속 로그 파일로 결합합니다.'),
('zh','catlogging.common.timestampStatic.text.3','此后缀模式用于根据实时日志文件的名称和位置检测较旧的滚动文件.
示例：如果每日滚动文件带有实时日志 <code>server.log</code> 和滚动文件,例如
<code>server.log.2015-07-02</code>, <code>server.log.2015-07-01</code> 等模式<code>.*</code>
将匹配旧的翻转文件并将所有文件组合成一个连续的日志文件.'),

('en','catlogging.common.orderRollFile.text','Defines the order criteria for rolled over files which should correlate to the sequence the log files were written over time.'),
('ko','catlogging.common.orderRollFile.text','로그 파일 이름 또는 시간에 상관되어야 하는 롤오버된 파일의 순서 기준을 정의합니다.'),
('zh','catlogging.common.orderRollFile.text','定义应与日志文件的顺序相关的翻转文件的顺序标准是随着时间写的.'),

('en','catlogging.common.absolutePath','Absolute path : ... "{0}" ... must be included.'),
('ko','catlogging.common.absolutePath','절대 경로: ... "{0}" ...이(가) 포함되어야 합니다.'),
('zh','catlogging.common.absolutePath','绝对路径 : ... "{0}" ... 必须包括在内.'),

('en','catlogging.common.timestampDynamic.label.1','Source for rolled log files with a live file named dynamically'),
('ko','catlogging.common.timestampDynamic.label.1','동적으로 명명된 라이브 파일이 있는 롤링된 로그 파일의 소스'),
('zh','catlogging.common.timestampDynamic.label.1','带有动态命名的活动文件的滚动日志文件的来源'),

('en','catlogging.common.timestampDynamic.text.1','This source concats a live actively written log file with older rolled over files into a single continuous log.
		The live log is the single file which can change size. After it''s rolled the size should be fix and no longer got changed to
		enable exact navigation in the connected logs. Due to the name of the live file is dynamic e.g. containing
		the current day timestamp, it''s determined as the first file of the matching file collection regarding
		the set order criteria.
		<strong>Important:</strong> This source supports rolling only based on a timestamp name pattern, because this guarantees
		static names of rolled over files, which is the key for navigating in the connected files.'),
('ko','catlogging.common.timestampDynamic.text.1','이 소스는 이전에 롤오버된 파일과 함께 활성 기록된 라이브 로그 파일을 단일 연속 로그로 연결합니다.
라이브 로그는 크기를 변경할 수 있는 단일 파일입니다. 롤링된 후에는 크기가 수정되어야 하며 더 이상 다음으로 변경되지 않습니다.
연결된 로그에서 정확한 탐색을 활성화합니다. 라이브 파일의 이름으로 인해 동적입니다. 함유
현재 날짜 타임스탬프, 다음과 관련하여 일치하는 파일 컬렉션의 첫 번째 파일로 결정됩니다.
주문 기준을 설정합니다.
<strong>중요:</strong> 이 소스는 타임스탬프 이름 패턴을 기반으로 한 롤링만 지원합니다.
연결된 파일을 탐색하기 위한 키인 롤오버된 파일의 정적 이름입니다.'),
('zh','catlogging.common.timestampDynamic.text.1','此源将实时主动写入的日志文件与较旧的滚动文件合并为单个连续日志.
实时日志是可以更改大小的单个文件. 卷起后,尺寸应该是固定的,不再更改为
在连接的日志中启用精确导航. 由于实时文件的名称是动态的,例如 包含
当前日期时间戳,它被确定为匹配文件集合的第一个文件
设置的订单标准.
<strong>重要提示</strong>：此来源仅支持基于时间戳名称模式的滚动,因为这可以保证
滚动文件的静态名称,这是在连接文件中导航的关键.'),

('en','catlogging.common.timestampDynamic.text.2','Reference here the live and the rolled over files using an
					<a href="http://ant.apache.org/manual/dirtasks.html#patterns" target="_blank">Ant-style pattern</a> expression.
					The live file is determined as first entry of the matching file collection regarding
					the set order criteria. This source supports exposing of only one rolling log.'),
('ko','catlogging.common.timestampDynamic.text.2','여기를 사용하여 라이브 및 롤오버 파일을 참조하십시오.
<a href="http://ant.apache.org/manual/dirtasks.html#patterns" target="_blank">Ant 스타일 패턴</a> 표현식입니다.
라이브 파일은 다음과 관련하여 일치하는 파일 컬렉션의 첫 번째 항목으로 결정됩니다.
주문 기준을 설정합니다. 이 소스는 하나의 롤링 로그 노출만 지원합니다.'),
('zh','catlogging.common.timestampDynamic.text.2','使用
<a href="http://ant.apache.org/manual/dirtasks.html#patterns" target="_blank">Ant 样式模式</a> 表达式.
活动文件被确定为匹配文件集合的第一个条目,关于
设置的订单标准. 此源仅支持公开一个滚动日志.'),

('en','catlogging.common.timestampDynamic.label.2','Path pattern for the live and rolled over files'),
('ko','catlogging.common.timestampDynamic.label.2','라이브 및 롤오버 파일의 경로 패턴'),
('zh','catlogging.common.timestampDynamic.label.2','实时和翻转文件的路径模式'),

('en','catlogging.common.compoundLogData','Log instances to combine'),
('ko','catlogging.common.compoundLogData','결합할 로그 인스턴스'),
('zh','catlogging.common.compoundLogData','要合并的日志实例'),

('en','catlogging.common.compoundLogData.text.1','Compounds log data from multiple logs into a continuous stream ordered by the timestamp'),
('ko','catlogging.common.compoundLogData.text.1','여러 로그의 로그 데이터를 타임스탬프로 정렬된 연속 스트림으로 합성합니다.'),
('zh','catlogging.common.compoundLogData.text.1','复合将来自多个日志的数据记录到按时间戳排序的连续流中.'),

('en','catlogging.common.compoundLogData.text.2','This source compounds in real-time multiple logs into one continuous stream of log entries
ordered by the internal timestamp field <code>lf_timestamp</code>. This enables correlation of logs from multiple sources by timestamp.
Typical use case are e.g.
<ul>
    <li>Compounding of logs from all hosts of a cluster</li>
    <li>Correlating of logs from different systems like web servers, application
        servers, databases etc. to support efficient root cause analysis</li>
</ul>
Make sure the entries in the log files to combine are ordered my a timestamp value which is parsed and evaluated as a date
field (e.g. using the timestamp filter).'),
('ko','catlogging.common.compoundLogData.text.2','이 소스는 실시간으로 여러 로그를 하나의 연속적인 로그 항목 스트림으로 합성합니다.
내부 타임스탬프 필드 <code>lf_timestamp</code>를 기준으로 정렬됩니다. 이를 통해 타임스탬프별로 여러 소스의 로그를 상관시킬 수 있습니다.
일반적인 사용 사례는 다음과 같습니다.
<ul>
<li>클러스터의 모든 호스트에서 로그 조합</li>
<li>웹 서버, 애플리케이션과 같은 다양한 시스템의 로그 상관 관계
효율적인 근본 원인 분석을 지원하기 위한 서버, 데이터베이스 등</li>
</ul>
결합할 로그 파일의 항목이 구문 분석되고 날짜로 평가되는 타임스탬프 값으로 정렬되었는지 확인하십시오.
필드(예: 타임스탬프 필터 사용).'),
('zh','catlogging.common.compoundLogData.text.2','此源将多个日志实时合成为一个连续的日志条目流
按内部时间戳字段 <code>lf_timestamp</code> 排序. 这可以通过时间戳关联来自多个来源的日志.
典型的用例是例如
<ul>
<li>来自集群所有主机的日志的混合</li>
<li>关联来自不同系统（如 Web 服务器,应用程序）的日志
服务器,数据库等 支持有效的根本原因分析</li>
</ul>
确保要合并的日志文件中的条目按时间戳值排序，该值被解析并评估为日期
字段（例如使用时间戳过滤器）.'),

('en','catlogging.common.compoundLogData.text.3','Choose between compounding all logs from a source or only a dedicated one.'),
('ko','catlogging.common.compoundLogData.text.3','소스의 모든 로그를 합성하거나 전용 로그만 합성할 수 있습니다.'),
('zh','catlogging.common.compoundLogData.text.3','选择混合来自一个源的所有日志还是仅合成一个专用日志.'),

-- # Sources
('en','catlogging.source.deleted','Log source "{0}" deleted successfully!'),
('ko','catlogging.source.deleted','로그 소스 "{0}" 성공적으로 삭제되었습니다!'),
('zh','catlogging.source.deleted','日志源 "{0}" 删除成功！'),

-- # Sniffers
('en','catlogging.sniffers.positioning.show','Shows the current monitoring position in the logs'),
('ko','catlogging.sniffers.positioning.show','로그에서 현재 모니터링 위치를 보여줍니다'),
('zh','catlogging.sniffers.positioning.show','在日志中显示当前监控位置'),

('en','catlogging.sniffers.positioning.set','Set the position in the logs to start monitoring from'),
('ko','catlogging.sniffers.positioning.set','로그에서 모니터링을 시작할 위치 설정'),
('zh','catlogging.sniffers.positioning.set','在日志中设置开始监控的位置'),

('en','catlogging.sniffers.scheduled.true','Active'),
('ko','catlogging.sniffers.scheduled.true','활성화'),
('zh','catlogging.sniffers.scheduled.true','活动'),

('en','catlogging.sniffers.scheduled.false','Stopped'),
('ko','catlogging.sniffers.scheduled.false','비활성화'),
('zh','catlogging.sniffers.scheduled.false','停止'),

('en','catlogging.sniffers.deleted','Logger "{0}" deleted successfully!'),
('ko','catlogging.sniffers.deleted','로거 "{0}" 성공적으로 삭제되었습니다!'),
('zh','catlogging.sniffers.deleted','记录器 "{0}" 删除成功！'),

-- # Labels
('en','catlogging.type.com.catlogging.event.Event','Event'),
('ko','catlogging.type.com.catlogging.event.Event','이벤트'),
('zh','catlogging.type.com.catlogging.event.Event','事件'),

('en','catlogging.type.com.catlogging.model.LogSource','Log source'),
('ko','catlogging.type.com.catlogging.model.LogSource','로그 소스'),
('zh','catlogging.type.com.catlogging.model.LogSource','日志源'),


-- # Exceptions
('en','catlogging.exception.404.short','{0} not found'),
('ko','catlogging.exception.404.short','{0} 페이지를 찾을 수 없습니다.'),
('zh','catlogging.exception.404.short','{0} 未找到'),

('en','catlogging.exception.404.detail','{0} for ID "{1}" not found. Probably it''s already deleted or you URL is wrong.'),
('ko','catlogging.exception.404.detail','{0} 의 ID "{1}"을(를) 찾을 수 없습니다. 이미 삭제되었거나 URL이 잘못되었을 수 있습니다.'),
('zh','catlogging.exception.404.detail','{0} 未找到 ID "{1}". 可能它已经被删除或者你的 URL 错误.'),

('en','catlogging.exception.refintg.short','Deletion error'),
('ko','catlogging.exception.refintg.short','삭제 오류'),
('zh','catlogging.exception.refintg.short','删除错误'),

('en','catlogging.exception.refintg.detail','{0} couldn''t be deleted due to references. To continue you have first to check for references and to remove these.'),
('ko','catlogging.exception.refintg.detail','{0} 은(는) 참조로 인해 삭제할 수 없습니다. 계속하려면 먼저 참조를 확인하고 제거해야 합니다.'),
('zh','catlogging.exception.refintg.detail','{0} 由于引用无法删除. 要继续，您必须先检查引用并删除它们.'),

-- # Breadcrumb
('en','catlogging.breadcrumb.eventObserver','Event Observer'),
('ko','catlogging.breadcrumb.eventObserver','이벤트 감시자'),
('zh','catlogging.breadcrumb.eventObserver','事件观察者'),

('en','catlogging.breadcrumb.sources','Log sources'),
('ko','catlogging.breadcrumb.sources','로그 소스'),
('zh','catlogging.breadcrumb.sources','日志源'),

('en','catlogging.breadcrumb.sniffers','Logger'),
('ko','catlogging.breadcrumb.sniffers','로거'),
('zh','catlogging.breadcrumb.sniffers','记录器'),

('en','catlogging.breadcrumb.reports','Dashboards'),
('ko','catlogging.breadcrumb.reports','대시보드'),
('zh','catlogging.breadcrumb.reports','仪表板'),

('en','catlogging.breadcrumb.settings','Settings'),
('ko','catlogging.breadcrumb.settings','설정'),
('zh','catlogging.breadcrumb.settings','设置'),

-- # Confirm dialogs
('en','catlogging.confirms.delete','Delete really?'),
('ko','catlogging.confirms.delete','정말로 삭제하겠습니까?'),
('zh','catlogging.confirms.delete','真的删了吗?'),

-- # Nav
('en', 'catlogging.nav.logs', 'Logs'),
('ko', 'catlogging.nav.logs', '로그'),
('zh', 'catlogging.nav.logs', '日志'),

('en', 'catlogging.nav.events', 'Events'),
('ko', 'catlogging.nav.events', '이벤트'),
('zh', 'catlogging.nav.events', '活动'),

('en', 'catlogging.nav.system', 'System'),
('ko', 'catlogging.nav.system', '설정'),
('zh', 'catlogging.nav.system', '系统'),

-- # Lang
('en', 'catlogging.lang.en','English'),
('ko', 'catlogging.lang.en','영어'),
('zh', 'catlogging.lang.en','英語'),

('en', 'catlogging.lang.ko','Korea'),
('ko', 'catlogging.lang.ko','한국어'),
('zh', 'catlogging.lang.ko','韩国人'),

('en', 'catlogging.lang.zh','Chinese'),
('ko', 'catlogging.lang.zh','중국어'),
('zh', 'catlogging.lang.zh','普通話');
