from src.core.config import CoffeeAndNoodlesConfig
from src.core.web.web_of_trust.wot import wot_reports_for_domains, parse_attributes_for_report

config = CoffeeAndNoodlesConfig()

report = wot_reports_for_domains("google.com", config.wot_key)
print(parse_attributes_for_report(report))

report = wot_reports_for_domains(["yahoo.com"],config.wot_key)
print(parse_attributes_for_report(report["yahoo.com"]))