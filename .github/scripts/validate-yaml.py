#!/usr/bin/env python3
"""Validate YAML syntax for CI/CD files"""

import yaml
import sys

def validate_yaml(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            data = yaml.safe_load(f)
        print(f"[OK] {filepath} - YAML syntax is valid")
        if 'jobs' in data:
            print(f"  Found {len(data['jobs'])} jobs")
        return True
    except yaml.YAMLError as e:
        print(f"[ERROR] {filepath} - YAML syntax error:")
        print(f"  {e}")
        return False
    except Exception as e:
        print(f"[ERROR] {filepath} - Error:")
        print(f"  {e}")
        return False

def main():
    files = [
        '.github/workflows/cicd-pipeline.yml',
        '.zap/zap-config.yaml'
    ]
    
    print("="*50)
    print("YAML Syntax Validation")
    print("="*50)
    
    all_valid = True
    for filepath in files:
        if not validate_yaml(filepath):
            all_valid = False
        print()
    
    if all_valid:
        print("All YAML files are valid!")
        sys.exit(0)
    else:
        print("Some YAML files have errors!")
        sys.exit(1)

if __name__ == '__main__':
    main()
