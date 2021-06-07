package evgenykuzin.core.cnfg;

public class TableConfig {
    public static class AdditionalOzonDocFieldsConfig {
        public static final String LOWER_PRICE_COL_NAME = "Проценка";
        public static final String DIFF_PRICES_COL_NAME = "Разница с проценкой";
        public static final String CONCURRENT_URL_COL_NAME = "ссылка на конкурента";
        public static final String SEARCH_BARCODE_COL_NAME = "Barcode for Search";
        public static final String SUPPLIER_COL_NAME = "Поставщик";
    }

    public static class OzonDocConfig {
        public static final String OZON_PRODUCT_ID_COL_NAME = "Ozon Product ID";
        public static final String PRICE_COL_NAME = "Текущая цена с учетом скидки, руб.";
        public static final String NAME_COL_NAME = "Наименование товара";
        public static final String BRAND_COL_NAME = "Бренд";
        public static final String ARTICLE_COL_NAME = "Артикул";
        public static final String BARCODE_COL_NAME = "Barcode";
        public static final String STOCKS_COL_NAME = "Доступно на моем складе, шт";
        public static final String SKU_FBS_COL_NAME = "FBS OZON SKU ID";
        public static final String SKU_FBO_COL_NAME = "FBO OZON SKU ID";
    }

    public static class XmarketConfig {
        public static final String ID_COL_NAME = "ELEMENT_ID";
        public static final String PRICE_COL_NAME = "Цена клиента";
        public static final String NAME_COL_NAME = "Название";
        public static final String BRAND_COL_NAME = "ТМ";
        public static final String ARTICLE_COL_NAME = "Артикул";
        public static final String BARCODE_COL_NAME = "Штрихкод";
        public static final String STOCKS_COL_NAME = "Остаток на складе";
    }

    public static class MiragToysConfig {
        public static final String ID_COL_NAME = "Код";
        public static final String NAME_COL_NAME = "Название";
        public static final String BRAND_COL_NAME = "Изготовитель";
        public static final String PRICE_COL_NAME = "Оптовая цена";
        public static final String ARTICLE_COL_NAME = "Артикул";
        public static final String STOCKS_COL_NAME = "Остаток";
    }

    public static class ZooekspressConfig {
        public static final String ID_COL_NAME = "ID";
        public static final String NAME_COL_NAME = "Наименование";
        public static final String BRAND_COL_NAME = "Изготовитель";
        public static final String PRICE_COL_NAME = "Цена за шт., руб.";
        public static final String ARTICLE_COL_NAME = "Артикул";
        public static final String PACKAGE_STOCK = "Кол-во в упак.";
    }

}
