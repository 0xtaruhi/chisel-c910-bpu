RTL_DIR = ../gen_rtl
SRC = $(wildcard $(RTL_DIR)/*.v)

.PHONY: rename

rename: $(SRC)
	@echo "Renaming IO Ports ..."
	@./rename_io.sh $^
	@echo "Done"
