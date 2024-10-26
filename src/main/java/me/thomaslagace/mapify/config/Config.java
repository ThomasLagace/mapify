package me.thomaslagace.mapify.config;

import java.util.List;

/**
 * The configuration of the application.
 */
public class Config {
    public static final int MAX_MAPS_COUNT_BY_USER = 5;
    public static final int MAX_IMAGE_WIDTH = 1920;
    public static final int MAX_IMAGE_HEIGHT = 1920;
    public static final int MAX_MAPS_WIDTH = 10;
    public static final int MAX_MAPS_HEIGHT = 10;
    public static final String IMAGE_STORAGE_PATH = "mapify/images/";
    public static List<String> ALLOWED_IMAGE_DOMAINS = List.of();

    public static final String MAP_NOT_FOUND = "Map not found";
    public static final String ITEM_FRAME_NOT_FOUND = "Item frame not found";
    public static final String ITEM_FRAMES_NOT_EMPTY = "Some item frames are not empty";
    public static final String MUST_BE_PLAYER = "You must be a player to run this command";
    public static final String INVALID_URL = "This is not a valid URL";
    public static final String INVALID_DOMAIN = "This domain is not allowed";
    public static final String CREATING_IMAGE = "Creating the image...";
    public static final String IMAGE_CREATED = "The image has been created!";
    public static final String IMAGE_NOT_FOUND = "The image was not found";
    public static final String CREATED_BY = "Created by ";
    public static final String IMAGE_ALREADY_EXISTS = "Image is already created with this name";
    public static final String CURRENTLY_NO_IMAGES = "You currently have no images";
    public static final String CURRENTLY_IMAGES_COUNT = "Current image count : %d / %d";
    public static final String USER_MAP_LIMIT_REACHED = "You have reached the maximum number of images";
    public static final String IMAGE_REMOVED = "The image has been removed";

    public static final String MAPIFY_ROOT_COMMAND = "mapify";
    public static final String MAPIFY_CREATE_ARGUMENT_COMMAND = "create";
    public static final String MAPIFY_WIDTH_ARGUMENT_COMMAND = "width";
    public static final String MAPIFY_HEIGHT_ARGUMENT_COMMAND = "height";
    public static final String MAPIFY_NAME_ARGUMENT_COMMAND = "name";
    public static final String MAPIFY_URL_ARGUMENT_COMMAND = "url";
    public static final String MAPIFY_GET_ARGUMENT_COMMAND = "get";
    public static final String MAPIFY_LIST_ARGUMENT_COMMAND = "list";
    public static final String MAPIFY_REMOVE_ARGUMENT_COMMAND = "remove";
}
