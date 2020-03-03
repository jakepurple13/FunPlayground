prefix = "\u001B"
RESET = f"{prefix}[0m"


def _getColor(r: int, g: int, b: int):
    return f"[38;2;{r};{g};{b}"


def _regularColorInt(r: int, g: int, b: int):
    return f"{prefix}{_getColor(r, g, b)}m"


def _regularColorColor(color: int):
    colors = _intToColor(color)
    return f"{prefix}{_getColor(colors[0], colors[1], colors[2])}m"


def colorText(string: str, r: int, g: int, b: int):
    return f"{_regularColorInt(r, g, b)}{string}{RESET}"


def colorText(string: str, color: int):
    return f"{_regularColorColor(color)}{string}{RESET}"


def _intToColor(integer):
    r = integer >> 16 & 0xff
    g = integer >> 8 & 0xff
    b = integer & 0xff
    return r, g, b
